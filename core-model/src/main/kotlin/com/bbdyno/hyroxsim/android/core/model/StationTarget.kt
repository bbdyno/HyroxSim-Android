//
//  StationTarget.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable
import kotlin.math.floor

sealed interface StationTarget : Serializable {
    val formatted: String

    data class Distance(val meters: Double) : StationTarget {
        override val formatted: String = "${meters.toInt()} m"
    }

    data class Reps(val count: Int) : StationTarget {
        override val formatted: String = "$count reps"
    }

    data class Duration(val seconds: Double) : StationTarget {
        override val formatted: String = Companion.formatDuration(seconds)
    }

    data object None : StationTarget {
        override val formatted: String = "—"
    }

    companion object {
        fun distance(meters: Double): StationTarget = Distance(meters)
        fun reps(count: Int): StationTarget = Reps(count)
        fun duration(seconds: Double): StationTarget = Duration(seconds)
        fun none(): StationTarget = None

        private fun formatDuration(seconds: Double): String {
            val totalSeconds = floor(seconds).toInt().coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val remainder = totalSeconds % 60
            return "%02d:%02d".format(minutes, remainder)
        }
    }
}
