package com.bbdyno.hyroxsim.core.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User-set target time for a specific template. Distinct from the watch's
 * `GoalStore` (which mirrors the phone's most recent push).
 *
 * `targetSegmentsMsJson`: serialised `List<Long>` (one entry per template
 * segment). Optional — when null, `PaceReference` fallback applies.
 */
@Entity(tableName = "goal")
data class StoredGoalEntity(
    @PrimaryKey val templateId: String,
    val targetTotalMs: Long,
    val targetSegmentsMsJson: String?,
    val updatedAtEpochMs: Long,
)
