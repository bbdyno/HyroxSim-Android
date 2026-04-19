package com.bbdyno.hyroxsim.core.persistence.repository

import com.bbdyno.hyroxsim.core.persistence.dao.GoalDao
import com.bbdyno.hyroxsim.core.persistence.entities.StoredGoalEntity
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.Instant

data class Goal(
    val templateId: String,
    val targetTotalMs: Long,
    val targetSegmentsMs: List<Long>?,
)

class GoalRepository(private val dao: GoalDao) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun save(goal: Goal) {
        dao.upsert(
            StoredGoalEntity(
                templateId = goal.templateId,
                targetTotalMs = goal.targetTotalMs,
                targetSegmentsMsJson = goal.targetSegmentsMs?.let {
                    json.encodeToString(ListSerializer(Long.serializer()), it)
                },
                updatedAtEpochMs = Instant.now().toEpochMilli(),
            )
        )
    }

    suspend fun find(templateId: String): Goal? {
        val e = dao.findByTemplate(templateId) ?: return null
        val segs = e.targetSegmentsMsJson?.let {
            runCatching {
                json.decodeFromString(ListSerializer(Long.serializer()), it)
            }.getOrNull()
        }
        return Goal(
            templateId = e.templateId,
            targetTotalMs = e.targetTotalMs,
            targetSegmentsMs = segs,
        )
    }

    suspend fun delete(templateId: String) = dao.delete(templateId)
}
