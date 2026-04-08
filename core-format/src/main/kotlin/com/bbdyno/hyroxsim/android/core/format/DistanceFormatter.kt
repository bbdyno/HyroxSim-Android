//
//  DistanceFormatter.kt
//  core-format
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.format

object DistanceFormatter {
    fun short(meters: Double): String =
        if (meters >= 1000.0) {
            "%.2f km".format(meters / 1000.0)
        } else {
            "${meters.toInt()} m"
        }
}
