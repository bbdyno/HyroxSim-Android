package com.bbdyno.hyroxsim.core.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores user-built custom workout templates. The full `segments` list is
 * serialised to a JSON blob to avoid a second table — templates are
 * typically read/written whole, and a 31-segment preset is ~4 KB.
 */
@Entity(tableName = "template")
data class StoredTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val divisionRaw: String?,
    val usesRoxZone: Boolean,
    val createdAtEpochMs: Long,
    val isBuiltIn: Boolean,
    val segmentsJson: String,
)
