//
//  CompletedWorkout.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

data class CompletedWorkout(
    val id: UUID = UUID.randomUUID(),
    val templateName: String,
    val division: HyroxDivision? = null,
    val startedAt: Instant,
    val finishedAt: Instant,
    val segments: List<SegmentRecord>,
) : Serializable {

    val totalDuration: Double
        get() = ChronoUnit.MILLIS.between(startedAt, finishedAt).toDouble() / 1000.0

    val totalActiveDuration: Double
        get() = segments.sumOf { it.activeDuration }

    val totalDistanceMeters: Double
        get() = segments.sumOf { it.distanceMeters }

    val runSegments: List<SegmentRecord>
        get() = segments.filter { it.type == SegmentType.RUN }

    val roxZoneSegments: List<SegmentRecord>
        get() = segments.filter { it.type == SegmentType.ROX_ZONE }

    val stationSegments: List<SegmentRecord>
        get() = segments.filter { it.type == SegmentType.STATION }

    val averageHeartRate: Int?
        get() {
            val samples = segments.flatMap { it.measurements.heartRateSamples }
            if (samples.isEmpty()) return null
            return samples.sumOf { it.bpm } / samples.size
        }

    val maxHeartRate: Int?
        get() = segments.mapNotNull { it.measurements.maxHeartRate }.maxOrNull()

    val averageRunPaceSecondsPerKm: Double?
        get() {
            val runAndRox = segments.filter { it.type == SegmentType.RUN || it.type == SegmentType.ROX_ZONE }
            val totalDistance = runAndRox.sumOf { it.distanceMeters }
            val totalActive = runAndRox.sumOf { it.activeDuration }
            val kilometers = totalDistance / 1000.0
            if (kilometers <= 0.001) return null
            return totalActive / kilometers
        }

    fun resolvedStationDisplayName(record: SegmentRecord): String? {
        if (record.type != SegmentType.STATION) return null
        if (!record.stationDisplayName.isNullOrEmpty()) return record.stationDisplayName

        val resolvedDivision = division ?: return null
        val ordinal = stationOrdinal(record) ?: return null
        val stations = HyroxDivisionSpec.stations(resolvedDivision)
        if (ordinal !in stations.indices) return null
        return stations[ordinal].kind.displayName
    }

    private fun stationOrdinal(record: SegmentRecord): Int? {
        var ordinal = 0
        for (segment in segments) {
            if (segment.type != SegmentType.STATION) continue
            val sameRecord = segment.id == record.id ||
                (segment.segmentId == record.segmentId && segment.index == record.index) ||
                segment.index == record.index
            if (sameRecord) return ordinal
            ordinal += 1
        }
        return null
    }
}
