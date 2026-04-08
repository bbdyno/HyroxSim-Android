//
//  WorkoutTemplate.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable
import java.time.Instant
import java.util.UUID

data class WorkoutTemplate(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val division: HyroxDivision? = null,
    val segments: List<WorkoutSegment>,
    val createdAt: Instant = Instant.now(),
    val isBuiltIn: Boolean = false,
) : Serializable {

    val totalRunDistanceMeters: Double
        get() = segments
            .filter { it.type == SegmentType.RUN }
            .mapNotNull { it.distanceMeters }
            .sum()

    val stationCount: Int
        get() = segments.count { it.type == SegmentType.STATION }

    val estimatedDurationSeconds: Double
        get() = segments.sumOf { segment ->
            when (segment.type) {
                SegmentType.RUN -> (segment.distanceMeters ?: 1000.0) * 0.36
                SegmentType.ROX_ZONE -> 30.0
                SegmentType.STATION -> 240.0
            }
        }

    fun validate() {
        if (segments.isEmpty()) {
            throw WorkoutTemplateValidationException
        }
        segments.forEach { it.validate() }
    }
}

data object WorkoutTemplateValidationException :
    IllegalArgumentException("Workout template must have at least one segment"),
    Serializable
