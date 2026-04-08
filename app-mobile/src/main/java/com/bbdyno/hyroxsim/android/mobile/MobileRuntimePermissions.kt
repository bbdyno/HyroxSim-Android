//
//  MobileRuntimePermissions.kt
//  app-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.mobile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate

internal fun Context.missingPhoneWorkoutPermissions(
    template: WorkoutTemplate,
): List<String> =
    buildList {
        if (template.totalRunDistanceMeters > 0.0) {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }.distinct().filterNot(::hasPermission)

internal fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
