package com.bbdyno.hyroxsim.core.persistence.repository

import com.bbdyno.hyroxsim.core.domain.CompletedWorkout
import com.bbdyno.hyroxsim.core.persistence.dao.WorkoutDao
import com.bbdyno.hyroxsim.core.persistence.mapper.CompletedWorkoutMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single read/write facade over [WorkoutDao]. Callers deal in domain
 * [CompletedWorkout] values — Room entities stay encapsulated here.
 */
class WorkoutRepository(private val dao: WorkoutDao) {

    suspend fun save(workout: CompletedWorkout) {
        val (entity, segments) = CompletedWorkoutMapper.toEntities(workout)
        dao.upsertWorkoutWithSegments(entity, segments)
    }

    suspend fun findById(id: String): CompletedWorkout? {
        val w = dao.findById(id) ?: return null
        val segs = dao.segmentsFor(id)
        return CompletedWorkoutMapper.fromEntities(w, segs)
    }

    fun observeAllSummaries(): Flow<List<WorkoutSummary>> =
        dao.observeAll().map { rows ->
            rows.map {
                WorkoutSummary(
                    id = it.id,
                    templateName = it.templateName,
                    divisionRaw = it.divisionRaw,
                    finishedAtEpochMs = it.finishedAtEpochMs,
                    totalActiveDurationMs = it.totalActiveDurationMs,
                    totalDistanceMeters = it.totalDistanceMeters,
                    sourceRaw = it.sourceRaw,
                )
            }
        }

    suspend fun delete(id: String) = dao.deleteById(id)
}

/** Light projection used in history list screens — avoids loading full measurements. */
data class WorkoutSummary(
    val id: String,
    val templateName: String,
    val divisionRaw: String?,
    val finishedAtEpochMs: Long,
    val totalActiveDurationMs: Long,
    val totalDistanceMeters: Double,
    val sourceRaw: String,
)
