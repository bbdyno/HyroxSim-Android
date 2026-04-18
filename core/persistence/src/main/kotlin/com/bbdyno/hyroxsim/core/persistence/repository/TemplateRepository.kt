package com.bbdyno.hyroxsim.core.persistence.repository

import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import com.bbdyno.hyroxsim.core.domain.WorkoutSegment
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate
import com.bbdyno.hyroxsim.core.persistence.dao.TemplateDao
import com.bbdyno.hyroxsim.core.persistence.entities.StoredTemplateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Domain-level facade over [TemplateDao]. Segments round-trip as a JSON
 * blob so the Room schema stays flat — same trade-off as segments/workout.
 *
 * Listeners that need to keep the Garmin watch in sync can observe
 * [observeAll] and forward updates through `GarminTemplateSyncService`.
 */
class TemplateRepository(private val dao: TemplateDao) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun save(template: WorkoutTemplate) {
        dao.upsert(
            StoredTemplateEntity(
                id = template.id,
                name = template.name,
                divisionRaw = template.division?.raw,
                usesRoxZone = template.usesRoxZone,
                createdAtEpochMs = template.createdAtEpochMs,
                isBuiltIn = template.isBuiltIn,
                segmentsJson = json.encodeToString(
                    ListSerializer(WorkoutSegment.serializer()),
                    template.segments,
                ),
            )
        )
    }

    suspend fun findById(id: String): WorkoutTemplate? =
        dao.findById(id)?.let(::fromEntity)

    fun observeAll(): Flow<List<WorkoutTemplate>> =
        dao.observeAll().map { rows -> rows.map(::fromEntity) }

    suspend fun delete(id: String) = dao.deleteById(id)

    private fun fromEntity(entity: StoredTemplateEntity): WorkoutTemplate {
        val segments = runCatching {
            json.decodeFromString(
                ListSerializer(WorkoutSegment.serializer()),
                entity.segmentsJson,
            )
        }.getOrDefault(emptyList())
        return WorkoutTemplate(
            id = entity.id,
            name = entity.name,
            division = entity.divisionRaw?.let(HyroxDivision::fromRaw),
            segments = segments,
            usesRoxZone = entity.usesRoxZone,
            createdAtEpochMs = entity.createdAtEpochMs,
            isBuiltIn = entity.isBuiltIn,
        )
    }
}
