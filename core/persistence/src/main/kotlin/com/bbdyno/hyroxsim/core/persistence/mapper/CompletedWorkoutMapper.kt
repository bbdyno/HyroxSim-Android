package com.bbdyno.hyroxsim.core.persistence.mapper

import com.bbdyno.hyroxsim.core.domain.CompletedWorkout
import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import com.bbdyno.hyroxsim.core.domain.SegmentMeasurements
import com.bbdyno.hyroxsim.core.domain.SegmentRecord
import com.bbdyno.hyroxsim.core.domain.SegmentType
import com.bbdyno.hyroxsim.core.domain.WorkoutSource
import com.bbdyno.hyroxsim.core.persistence.entities.StoredSegmentEntity
import com.bbdyno.hyroxsim.core.persistence.entities.StoredWorkoutEntity
import kotlinx.serialization.json.Json

/**
 * Converts between domain [CompletedWorkout] and Room-backed
 * [StoredWorkoutEntity] + [StoredSegmentEntity]. Measurements are stored as
 * a JSON blob since the samples table would be very large and is mostly
 * read as a whole.
 */
object CompletedWorkoutMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun toEntities(
        workout: CompletedWorkout,
    ): Pair<StoredWorkoutEntity, List<StoredSegmentEntity>> {
        val stored = StoredWorkoutEntity(
            id = workout.id,
            templateName = workout.templateName,
            divisionRaw = workout.division?.raw,
            startedAtEpochMs = workout.startedAtEpochMs,
            finishedAtEpochMs = workout.finishedAtEpochMs,
            sourceRaw = workout.source.raw,
            totalActiveDurationMs = workout.totalActiveDurationMs,
            totalDistanceMeters = workout.totalDistanceMeters,
            averageHeartRate = workout.averageHeartRate,
            maxHeartRate = workout.maxHeartRate,
        )
        val segments = workout.segments.map { record ->
            StoredSegmentEntity(
                id = record.id,
                workoutId = workout.id,
                segmentId = record.segmentId,
                index = record.index,
                typeRaw = record.type.raw,
                startedAtEpochMs = record.startedAtEpochMs,
                endedAtEpochMs = record.endedAtEpochMs,
                pausedDurationMs = record.pausedDurationMs,
                stationDisplayName = record.stationDisplayName,
                plannedDistanceMeters = record.plannedDistanceMeters,
                goalDurationSeconds = record.goalDurationSeconds,
                measurementsJson = json.encodeToString(
                    SegmentMeasurements.serializer(),
                    record.measurements,
                ),
            )
        }
        return stored to segments
    }

    fun fromEntities(
        workout: StoredWorkoutEntity,
        segments: List<StoredSegmentEntity>,
    ): CompletedWorkout {
        val records = segments
            .sortedBy { it.index }
            .map { seg ->
                val measurements = runCatching {
                    json.decodeFromString(SegmentMeasurements.serializer(), seg.measurementsJson)
                }.getOrDefault(SegmentMeasurements())
                SegmentRecord(
                    id = seg.id,
                    segmentId = seg.segmentId,
                    index = seg.index,
                    type = SegmentType.fromRaw(seg.typeRaw) ?: SegmentType.Run,
                    startedAtEpochMs = seg.startedAtEpochMs,
                    endedAtEpochMs = seg.endedAtEpochMs,
                    pausedDurationMs = seg.pausedDurationMs,
                    measurements = measurements,
                    stationDisplayName = seg.stationDisplayName,
                    plannedDistanceMeters = seg.plannedDistanceMeters,
                    goalDurationSeconds = seg.goalDurationSeconds,
                )
            }
        return CompletedWorkout(
            id = workout.id,
            templateName = workout.templateName,
            division = workout.divisionRaw?.let(HyroxDivision::fromRaw),
            startedAtEpochMs = workout.startedAtEpochMs,
            finishedAtEpochMs = workout.finishedAtEpochMs,
            source = WorkoutSource.fromRaw(workout.sourceRaw) ?: WorkoutSource.Manual,
            segments = records,
        )
    }
}
