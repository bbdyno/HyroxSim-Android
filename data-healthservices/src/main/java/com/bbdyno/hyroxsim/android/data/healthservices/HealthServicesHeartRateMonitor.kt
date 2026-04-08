//
//  HealthServicesHeartRateMonitor.kt
//  data-healthservices
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.healthservices

import android.content.Context
import android.os.SystemClock
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import java.time.Instant
import java.util.concurrent.Executor

class HealthServicesHeartRateMonitor(
    context: Context,
) : HeartRateMonitor {
    override var heartRateListener: HeartRateListener? = null
    override var availabilityListener: AvailabilityListener? = null

    private val appContext = context.applicationContext
    private val measureClient: MeasureClient = HealthServices.getClient(appContext).measureClient
    private val directExecutor = Executor { runnable -> runnable.run() }
    private val bootInstantProvider: () -> Instant = {
        Instant.ofEpochMilli(System.currentTimeMillis() - SystemClock.elapsedRealtime())
    }

    private val callback = object : MeasureCallback {
        override fun onAvailabilityChanged(
            dataType: androidx.health.services.client.data.DeltaDataType<*, *>,
            availability: Availability,
        ) {
            availabilityListener?.onAvailability(availability.toString())
        }

        override fun onDataReceived(data: DataPointContainer) {
            val point = data.getData(DataType.HEART_RATE_BPM).lastOrNull() ?: return
            val measuredAt = bootInstantProvider().plusMillis(point.timeDurationFromBoot.toMillis())
            heartRateListener?.onHeartRate(point.value.toInt(), measuredAt)
        }
    }

    override fun refreshSupport(
        onSuccess: (Boolean) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val future = measureClient.getCapabilitiesAsync()
        future.addListener(
            Runnable {
                runCatching {
                    val capabilities = future.get()
                    DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure
                }
                    .onSuccess(onSuccess)
                    .onFailure(onError)
            },
            directExecutor,
        )
    }

    override fun start(listener: ResultListener) {
        runCatching {
            measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)
        }
            .onSuccess { listener.onResult(Result.success(Unit)) }
            .onFailure { listener.onResult(Result.failure(it)) }
    }

    override fun stop(listener: ResultListener) {
        measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback)
            .addListener(
                Runnable {
                    runCatching { Unit }
                        .onSuccess { listener.onResult(Result.success(Unit)) }
                        .onFailure { listener.onResult(Result.failure(it)) }
                },
                directExecutor,
            )
    }
}
