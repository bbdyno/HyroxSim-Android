package com.bbdyno.hyroxsim.core.domain

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CompletedWorkout(
    val id: String = UUID.randomUUID().toString(),
    val templateName: String,
    val division: HyroxDivision? = null,
    val startedAtEpochMs: Long,
    val finishedAtEpochMs: Long,
    val source: WorkoutSource = WorkoutSource.Manual,
    val segments: List<SegmentRecord>,
) {
    val totalDurationMs: Long get() = finishedAtEpochMs - startedAtEpochMs
    val totalActiveDurationMs: Long get() = segments.sumOf { it.activeDurationMs }
    val totalDistanceMeters: Double get() = segments.sumOf { it.distanceMeters }

    val runSegments get() = segments.filter { it.type == SegmentType.Run }
    val roxZoneSegments get() = segments.filter { it.type == SegmentType.RoxZone }
    val stationSegments get() = segments.filter { it.type == SegmentType.Station }

    val averageHeartRate: Int?
        get() {
            val all = segments.flatMap { it.measurements.heartRateSamples }
            if (all.isEmpty()) return null
            return all.sumOf { it.bpm } / all.size
        }

    val maxHeartRate: Int?
        get() = segments.mapNotNull { it.measurements.maxHeartRate }.maxOrNull()

    val averageRunPaceSecondsPerKm: Double?
        get() {
            val runLike = segments.filter { it.type == SegmentType.Run || it.type == SegmentType.RoxZone }
            val totalDist = runLike.sumOf { it.distanceMeters }
            val totalActive = runLike.sumOf { it.activeDurationMs } / 1000.0
            val km = totalDist / 1000.0
            return if (km > 0.001) totalActive / km else null
        }

    fun resolvedStationDisplayName(record: SegmentRecord): String? {
        if (record.type != SegmentType.Station) return null
        record.stationDisplayName?.takeIf { it.isNotBlank() }?.let { return it }
        val d = division ?: return null
        val stations = HyroxDivisionSpec.stationsFor(d)
        val ordinal = stationOrdinal(record) ?: return null
        return stations.getOrNull(ordinal)?.kind?.displayName
    }

    private fun stationOrdinal(target: SegmentRecord): Int? {
        var ordinal = 0
        for (s in segments) {
            if (s.type != SegmentType.Station) continue
            if (s.id == target.id || s.index == target.index) return ordinal
            ordinal++
        }
        return null
    }
}
