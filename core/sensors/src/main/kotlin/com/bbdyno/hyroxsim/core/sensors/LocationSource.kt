package com.bbdyno.hyroxsim.core.sensors

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.bbdyno.hyroxsim.core.domain.LocationSample
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Emits [LocationSample]s at ~1 Hz using FusedLocationProviderClient.
 * Caller is responsible for holding `ACCESS_FINE_LOCATION` permission;
 * if missing the flow completes immediately.
 */
class LocationSource(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun samples(): Flow<LocationSample> = callbackFlow {
        if (!hasPermission()) {
            close()
            return@callbackFlow
        }
        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000L)
            .setMinUpdateIntervalMillis(500L)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    trySend(
                        LocationSample(
                            timestampEpochMs = loc.time,
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            altitude = if (loc.hasAltitude()) loc.altitude else null,
                            horizontalAccuracy = if (loc.hasAccuracy()) loc.accuracy.toDouble() else null,
                            speed = if (loc.hasSpeed()) loc.speed.toDouble() else null,
                        )
                    )
                }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }

    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}
