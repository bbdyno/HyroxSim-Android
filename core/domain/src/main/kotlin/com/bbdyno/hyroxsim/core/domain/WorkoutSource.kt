package com.bbdyno.hyroxsim.core.domain

import kotlinx.serialization.Serializable

/**
 * Origin of a completed workout. Raw values match iOS enum raw values for
 * cross-platform persistence/JSON compatibility.
 */
@Serializable
enum class WorkoutSource(val raw: String) {
    Watch("watch"),      // Apple Watch (iOS only — not used on Android)
    Manual("manual"),
    Garmin("garmin");

    val displayName: String
        get() = when (this) {
            Watch -> "Apple Watch"
            Manual -> "Manual"
            Garmin -> "Garmin"
        }

    companion object {
        fun fromRaw(raw: String): WorkoutSource? = entries.firstOrNull { it.raw == raw }
    }
}
