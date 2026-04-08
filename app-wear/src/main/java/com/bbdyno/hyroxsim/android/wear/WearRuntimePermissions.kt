//
//  WearRuntimePermissions.kt
//  app-wear
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.wear

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate

private const val readHeartRatePermission = "android.permission.health.READ_HEART_RATE"

internal fun Context.missingWatchWorkoutPermissions(
    template: WorkoutTemplate,
): List<String> =
    buildList {
        add(Manifest.permission.ACTIVITY_RECOGNITION)
        if (template.totalRunDistanceMeters > 0.0) {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        add(readHeartRatePermission)
        if (Build.VERSION.SDK_INT <= 35) {
            add(Manifest.permission.BODY_SENSORS)
        }
    }.distinct().filterNot(::hasPermission)

internal fun Context.missingWatchHeartRateRelayPermissions(): List<String> =
    buildList {
        add(readHeartRatePermission)
        if (Build.VERSION.SDK_INT <= 35) {
            add(Manifest.permission.BODY_SENSORS)
        }
    }.distinct().filterNot(::hasPermission)

private fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
