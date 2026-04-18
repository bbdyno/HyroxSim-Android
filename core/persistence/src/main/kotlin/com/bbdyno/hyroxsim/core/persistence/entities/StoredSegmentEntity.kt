package com.bbdyno.hyroxsim.core.persistence.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a completed segment. `measurementsJson` holds the raw
 * sensor data as a serialized `SegmentMeasurements` blob so Room schema
 * stays flat (matches iOS HyroxPersistenceApple.StoredSegment).
 */
@Entity(
    tableName = "segment",
    foreignKeys = [ForeignKey(
        entity = StoredWorkoutEntity::class,
        parentColumns = ["id"],
        childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("workoutId")],
)
data class StoredSegmentEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val segmentId: String,
    val index: Int,
    val typeRaw: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long,
    val pausedDurationMs: Long,
    val stationDisplayName: String?,
    val plannedDistanceMeters: Double?,
    val goalDurationSeconds: Double?,
    val measurementsJson: String,
)
