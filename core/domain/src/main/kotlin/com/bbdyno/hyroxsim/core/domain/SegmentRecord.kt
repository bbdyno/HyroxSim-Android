package com.bbdyno.hyroxsim.core.domain

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Result produced when a segment completes. Carries timing, raw
 * measurements, and display metadata for persistence/history.
 */
@Serializable
data class SegmentRecord(
    val id: String = UUID.randomUUID().toString(),
    val segmentId: String,
    val index: Int,
    val type: SegmentType,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long,
    val pausedDurationMs: Long = 0,
    val measurements: SegmentMeasurements = SegmentMeasurements(),
    val stationDisplayName: String? = null,
    val plannedDistanceMeters: Double? = null,
    val goalDurationSeconds: Double? = null,
) {
    /** Wall-clock duration (includes paused time), milliseconds. */
    val durationMs: Long get() = endedAtEpochMs - startedAtEpochMs
    /** Active exercise time (excludes paused), milliseconds. */
    val activeDurationMs: Long get() = durationMs - pausedDurationMs

    val distanceMeters: Double get() = measurements.distanceMeters
    val averageHeartRate: Int? get() = measurements.averageHeartRate
    val averagePaceSecondsPerKm: Double?
        get() = measurements.averagePaceSecondsPerKm(activeDurationMs / 1000.0)
}
