package com.bbdyno.hyroxsim.core.domain

import kotlinx.serialization.Serializable

/**
 * Exercise target for a station. Weight is handled separately on
 * [WorkoutSegment.weightKg] because it varies by division.
 */
@Serializable
sealed interface StationTarget {

    @Serializable
    data class Distance(val meters: Int) : StationTarget

    @Serializable
    data class Reps(val count: Int) : StationTarget

    @Serializable
    data class Duration(val seconds: Int) : StationTarget

    @Serializable
    data object None : StationTarget

    val formatted: String
        get() = when (this) {
            is Distance -> "$meters m"
            is Reps -> "$count reps"
            is Duration -> "%02d:%02d".format(seconds / 60, seconds % 60)
            None -> "—"
        }
}
