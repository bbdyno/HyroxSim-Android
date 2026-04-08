//
//  PhoneLocationTracker.kt
//  app-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.mobile

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import com.bbdyno.hyroxsim.android.core.model.LocationSample
import java.time.Instant

class PhoneLocationTracker(
    context: Context,
) {
    var onLocationSample: ((LocationSample) -> Unit)? = null

    private val appContext = context.applicationContext
    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val listener = LocationListener { location ->
        onLocationSample?.invoke(location.toLocationSample())
    }

    @SuppressLint("MissingPermission")
    fun start() {
        stop()
        val providers = locationManager.allProviders.orEmpty().toSet()
        sequenceOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { it in providers }
            .forEach { provider ->
                locationManager.getLastKnownLocation(provider)?.let { location ->
                    onLocationSample?.invoke(location.toLocationSample())
                }
                locationManager.requestLocationUpdates(
                    provider,
                    1_000L,
                    1f,
                    listener,
                    Looper.getMainLooper(),
                )
            }
    }

    fun stop() {
        runCatching {
            locationManager.removeUpdates(listener)
        }
    }
}

private fun Location.toLocationSample(): LocationSample =
    LocationSample(
        timestamp = Instant.ofEpochMilli(time.takeIf { it > 0L } ?: System.currentTimeMillis()),
        latitude = latitude,
        longitude = longitude,
        altitude = altitude.takeUnless { it.isNaN() },
        horizontalAccuracy = accuracy.toDouble(),
        speed = speed.toDouble().takeUnless { it.isNaN() },
        course = bearing.toDouble().takeUnless { it.isNaN() },
    )
