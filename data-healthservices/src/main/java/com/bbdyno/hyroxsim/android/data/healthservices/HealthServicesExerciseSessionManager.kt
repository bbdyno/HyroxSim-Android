//
//  HealthServicesExerciseSessionManager.kt
//  data-healthservices
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.healthservices

import android.content.Context
import android.os.SystemClock
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.BatchingMode
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.LocationData
import androidx.health.services.client.data.LocationAccuracy
import androidx.health.services.client.data.WarmUpConfig
import com.bbdyno.hyroxsim.android.core.model.LocationSample
import com.google.common.util.concurrent.ListenableFuture
import java.time.Instant
import java.util.concurrent.Executor
import kotlin.math.max

class HealthServicesExerciseSessionManager(
    context: Context,
) : ExerciseSessionManager {
    override var exerciseListener: ExerciseSessionListener? = null
    override var availabilityListener: ExerciseAvailabilityListener? = null

    private val appContext = context.applicationContext
    private val exerciseClient: ExerciseClient = HealthServices.getClient(appContext).exerciseClient
    private val directExecutor = Executor { runnable -> runnable.run() }
    private val bootInstantProvider: () -> Instant = {
        Instant.ofEpochMilli(System.currentTimeMillis() - SystemClock.elapsedRealtime())
    }

    private val callback = object : ExerciseUpdateCallback {
        override fun onRegistered() = Unit

        override fun onRegistrationFailed(throwable: Throwable) = Unit

        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            exerciseListener?.onExerciseUpdate(update.toSnapshot(bootInstantProvider()))
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) = Unit

        override fun onAvailabilityChanged(
            dataType: androidx.health.services.client.data.DataType<*, *>,
            availability: Availability,
        ) {
            availabilityListener?.onAvailabilityChanged(
                ExerciseAvailabilitySnapshot(
                    dataTypeName = dataType.name,
                    status = availability.toString(),
                ),
            )
        }
    }

    init {
        exerciseClient.setUpdateCallback(callback)
    }

    override fun refreshCapabilities(
        onSuccess: (WorkoutTrackingCapabilities) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val future = exerciseClient.getCapabilitiesAsync()
        future.addListener(
            Runnable {
                try {
                    val capabilities = future.get()
                    val runningCapabilities = capabilities
                        .takeIf { ExerciseType.RUNNING in it.supportedExerciseTypes }
                        ?.getExerciseTypeCapabilities(ExerciseType.RUNNING)
                    onSuccess(
                        WorkoutTrackingCapabilities(
                            supportsExerciseTracking = ExerciseType.RUNNING in capabilities.supportedExerciseTypes,
                            supportsHeartRate = runningCapabilities?.supportedDataTypes?.contains(DataType.HEART_RATE_BPM) == true,
                            supportsLocation = runningCapabilities?.supportedDataTypes?.contains(DataType.LOCATION) == true,
                            supportsHeartRateBatching = capabilities.supportedBatchingModeOverrides.contains(
                                BatchingMode.HEART_RATE_5_SECONDS,
                            ),
                        ),
                    )
                } catch (throwable: Throwable) {
                    onError(throwable)
                }
            },
            directExecutor,
        )
    }

    override fun prepareExercise(
        warmUpHeartRate: Boolean,
        warmUpLocation: Boolean,
        listener: ResultListener,
    ) {
        val warmUpDataTypes = buildSet {
            if (warmUpHeartRate) add(DataType.HEART_RATE_BPM)
            if (warmUpLocation) add(DataType.LOCATION)
        }
        if (warmUpDataTypes.isEmpty()) {
            listener.onResult(Result.success(Unit))
            return
        }
        val config = WarmUpConfig(
            ExerciseType.RUNNING,
            warmUpDataTypes,
        )
        exerciseClient.prepareExerciseAsync(config)
            .forwardTo(listener)
    }

    override fun startExercise(
        useGps: Boolean,
        listener: ResultListener,
    ) {
        val config = ExerciseConfig(
            exerciseType = ExerciseType.RUNNING,
            dataTypes = buildSet {
                add(DataType.HEART_RATE_BPM)
                add(DataType.DISTANCE_TOTAL)
                if (useGps) add(DataType.LOCATION)
            },
            isAutoPauseAndResumeEnabled = false,
            isGpsEnabled = useGps,
        )
        exerciseClient.startExerciseAsync(config)
            .forwardTo(listener)
    }

    override fun pauseExercise(listener: ResultListener) {
        exerciseClient.pauseExerciseAsync().forwardTo(listener)
    }

    override fun resumeExercise(listener: ResultListener) {
        exerciseClient.resumeExerciseAsync().forwardTo(listener)
    }

    override fun endExercise(listener: ResultListener) {
        exerciseClient.endExerciseAsync().forwardTo(listener)
    }

    private fun ListenableFuture<Void>.forwardTo(listener: ResultListener) {
        addListener(
            Runnable {
                runCatching { get() }
                    .onSuccess { listener.onResult(Result.success(Unit)) }
                    .onFailure { listener.onResult(Result.failure(it)) }
            },
            directExecutor,
        )
    }

    private fun ExerciseUpdate.toSnapshot(bootInstant: Instant): ExerciseSessionSnapshot {
        val metrics = latestMetrics
        val latestHeartRate = metrics.getData(DataType.HEART_RATE_BPM)
            .lastOrNull()
            ?.value
            ?.toInt()
        val totalDistanceMeters = metrics.getData(DataType.DISTANCE_TOTAL)
            ?.total
            ?.toDouble()
        val latestLocation = metrics.getData(DataType.LOCATION)
            .lastOrNull()
            ?.toLocationSample(bootInstant)
        val activeDurationSeconds = activeDurationCheckpoint
            ?.activeDuration
            ?.toMillis()
            ?.toDouble()
            ?.div(1000.0)
            ?: 0.0

        return ExerciseSessionSnapshot(
            stateLabel = exerciseStateInfo.state.toString(),
            activeDurationSeconds = max(0.0, activeDurationSeconds),
            totalDistanceMeters = totalDistanceMeters,
            latestHeartRateBpm = latestHeartRate,
            latestLocation = latestLocation,
            startTime = startTime,
        )
    }

    private fun androidx.health.services.client.data.SampleDataPoint<LocationData>.toLocationSample(
        bootInstant: Instant,
    ): LocationSample {
        val timestamp = bootInstant.plusMillis(timeDurationFromBoot.toMillis())
        val locationAccuracy = accuracy as? LocationAccuracy
        return LocationSample(
            timestamp = timestamp,
            latitude = value.latitude,
            longitude = value.longitude,
            altitude = value.altitude.takeUnless { it.isNaN() },
            horizontalAccuracy = locationAccuracy?.horizontalPositionErrorMeters ?: 0.0,
            speed = null,
            course = value.bearing.takeUnless { it.isNaN() },
        )
    }
}
