//
//  DurationFormatter.kt
//  core-format
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.format

import kotlin.math.max

object DurationFormatter {
    fun hms(seconds: Double): String {
        val total = max(0, seconds.toInt())
        val hours = total / 3600
        val minutes = (total % 3600) / 60
        val remainder = total % 60
        return "%d:%02d:%02d".format(hours, minutes, remainder)
    }

    fun ms(seconds: Double): String {
        val total = max(0, seconds.toInt())
        val minutes = total / 60
        val remainder = total % 60
        return "%02d:%02d".format(minutes, remainder)
    }

    fun pace(secondsPerKm: Double?): String {
        val value = secondsPerKm ?: return "—"
        if (!value.isFinite() || value <= 0.0) return "—"
        val total = value.toInt()
        val minutes = total / 60
        val remainder = total % 60
        return "%d'%02d\" /km".format(minutes, remainder)
    }
}
