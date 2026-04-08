//
//  SegmentRecord.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

data class SegmentRecord(
    val id: UUID = UUID.randomUUID(),
    val segmentId: UUID,
    val index: Int,
    val type: SegmentType,
    val startedAt: Instant,
    val endedAt: Instant,
    val pausedDuration: Double = 0.0,
    val measurements: SegmentMeasurements = SegmentMeasurements(),
    val stationDisplayName: String? = null,
    val plannedDistanceMeters: Double? = null,
) : Serializable {

    val duration: Double
        get() = ChronoUnit.MILLIS.between(startedAt, endedAt).toDouble() / 1000.0

    val activeDuration: Double
        get() = (duration - pausedDuration).coerceAtLeast(0.0)

    val distanceMeters: Double
        get() = measurements.distanceMeters

    val averagePaceSecondsPerKm: Double?
        get() = measurements.averagePaceSecondsPerKm(activeDuration)

    val averageHeartRate: Int?
        get() = measurements.averageHeartRate
}
