package com.bbdyno.hyroxsim.core.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a completed workout. Segments are stored as a child
 * relation (see [StoredSegmentEntity]) for cascade delete semantics.
 *
 * `sourceRaw` is a plain string to keep Room schema straightforward and
 * allow future values (e.g. "wear") without a full migration.
 */
@Entity(tableName = "workout")
data class StoredWorkoutEntity(
    @PrimaryKey val id: String,
    val templateName: String,
    /** HyroxDivision.raw, or null for custom templates */
    val divisionRaw: String?,
    val startedAtEpochMs: Long,
    val finishedAtEpochMs: Long,
    /** WorkoutSource.raw (watch/manual/garmin) */
    val sourceRaw: String,
    val totalActiveDurationMs: Long,
    val totalDistanceMeters: Double,
    val averageHeartRate: Int?,
    val maxHeartRate: Int?,
)
