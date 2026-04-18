package com.bbdyno.hyroxsim.core.domain

import kotlinx.serialization.Serializable

/**
 * Type of a workout segment.
 *
 * Raw string values are the interop contract — keep in sync with
 * iOS `SegmentType.swift` and Monkey C `source/Domain/SegmentType.mc`.
 */
@Serializable
enum class SegmentType(val raw: String) {
    Run("run"),
    RoxZone("roxZone"),
    Station("station");

    val tracksLocation: Boolean get() = this == Run || this == RoxZone
    val tracksHeartRate: Boolean get() = true

    companion object {
        fun fromRaw(raw: String): SegmentType? = entries.firstOrNull { it.raw == raw }
    }
}
