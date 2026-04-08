//
//  WorkoutEngine.kt
//  core-engine
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.engine

import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.core.model.HeartRateSample
import com.bbdyno.hyroxsim.android.core.model.LocationSample
import com.bbdyno.hyroxsim.android.core.model.SegmentMeasurements
import com.bbdyno.hyroxsim.android.core.model.SegmentRecord
import com.bbdyno.hyroxsim.android.core.model.WorkoutSegment
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong

class WorkoutEngine(
    val template: WorkoutTemplate,
) {
    var state: EngineState = EngineState.Idle
        private set

    private val mutableRecords = mutableListOf<SegmentRecord>()
    val records: List<SegmentRecord>
        get() = mutableRecords.toList()

    private var currentSegmentPausedDuration: Double = 0.0
    private var liveMeasurements: SegmentMeasurements = SegmentMeasurements()

    val currentSegment: WorkoutSegment?
        get() = currentSegmentIndex?.let { template.segments[it] }

    val currentSegmentIndex: Int?
        get() = when (val current = state) {
            is EngineState.Running -> current.currentIndex
            is EngineState.Paused -> current.currentIndex
            EngineState.Idle, is EngineState.Finished -> null
        }

    val nextSegment: WorkoutSegment?
        get() {
            val index = currentSegmentIndex ?: return null
            return template.segments.getOrNull(index + 1)
        }

    val isLastSegment: Boolean
        get() = currentSegmentIndex == template.segments.lastIndex

    val isFinished: Boolean
        get() = state is EngineState.Finished

    fun segmentElapsed(now: Instant): Double =
        when (val current = state) {
            is EngineState.Running -> secondsBetween(current.segmentStartedAt, now)
            is EngineState.Paused -> current.segmentElapsed
            EngineState.Idle, is EngineState.Finished -> 0.0
        }

    fun totalElapsed(now: Instant): Double =
        when (val current = state) {
            is EngineState.Running -> secondsBetween(current.workoutStartedAt, now)
            is EngineState.Paused -> current.totalElapsed
            is EngineState.Finished -> secondsBetween(current.workoutStartedAt, current.finishedAt)
            EngineState.Idle -> 0.0
        }

    fun start(now: Instant) {
        if (state != EngineState.Idle) {
            throw EngineError.InvalidTransition(stateLabel, "start")
        }
        if (template.segments.isEmpty()) {
            throw EngineError.EmptyTemplate
        }
        currentSegmentPausedDuration = 0.0
        liveMeasurements = SegmentMeasurements()
        mutableRecords.clear()
        state = EngineState.Running(
            currentIndex = 0,
            segmentStartedAt = now,
            workoutStartedAt = now,
        )
    }

    fun advance(now: Instant) {
        val current = state as? EngineState.Running
            ?: throw EngineError.InvalidTransition(stateLabel, "advance")
        appendCurrentRecord(
            index = current.currentIndex,
            segmentStartedAt = current.segmentStartedAt,
            endedAt = now,
        )

        val nextIndex = current.currentIndex + 1
        if (nextIndex < template.segments.size) {
            currentSegmentPausedDuration = 0.0
            liveMeasurements = SegmentMeasurements()
            state = EngineState.Running(
                currentIndex = nextIndex,
                segmentStartedAt = now,
                workoutStartedAt = current.workoutStartedAt,
            )
        } else {
            currentSegmentPausedDuration = 0.0
            liveMeasurements = SegmentMeasurements()
            state = EngineState.Finished(
                workoutStartedAt = current.workoutStartedAt,
                finishedAt = now,
            )
        }
    }

    fun undo(_now: Instant) {
        when (val current = state) {
            is EngineState.Running -> {
                val lastRecord = mutableRecords.removeLastOrNull() ?: throw EngineError.NothingToUndo
                currentSegmentPausedDuration = 0.0
                liveMeasurements = SegmentMeasurements()
                state = EngineState.Running(
                    currentIndex = lastRecord.index,
                    segmentStartedAt = lastRecord.startedAt,
                    workoutStartedAt = current.workoutStartedAt,
                )
            }

            is EngineState.Finished -> {
                val lastRecord = mutableRecords.removeLastOrNull() ?: throw EngineError.NothingToUndo
                currentSegmentPausedDuration = 0.0
                liveMeasurements = SegmentMeasurements()
                state = EngineState.Running(
                    currentIndex = lastRecord.index,
                    segmentStartedAt = lastRecord.startedAt,
                    workoutStartedAt = current.workoutStartedAt,
                )
            }

            else -> throw EngineError.InvalidTransition(stateLabel, "undo")
        }
    }

    fun pause(now: Instant) {
        val current = state as? EngineState.Running
            ?: throw EngineError.InvalidTransition(stateLabel, "pause")
        state = EngineState.Paused(
            currentIndex = current.currentIndex,
            segmentElapsed = secondsBetween(current.segmentStartedAt, now),
            totalElapsed = secondsBetween(current.workoutStartedAt, now),
        )
    }

    fun resume(now: Instant) {
        val current = state as? EngineState.Paused
            ?: throw EngineError.InvalidTransition(stateLabel, "resume")
        currentSegmentPausedDuration = 0.0
        state = EngineState.Running(
            currentIndex = current.currentIndex,
            segmentStartedAt = backDate(now, current.segmentElapsed),
            workoutStartedAt = backDate(now, current.totalElapsed),
        )
    }

    fun finish(now: Instant) {
        when (val current = state) {
            is EngineState.Running -> {
                appendCurrentRecord(
                    index = current.currentIndex,
                    segmentStartedAt = current.segmentStartedAt,
                    endedAt = now,
                )
                currentSegmentPausedDuration = 0.0
                liveMeasurements = SegmentMeasurements()
                state = EngineState.Finished(
                    workoutStartedAt = current.workoutStartedAt,
                    finishedAt = now,
                )
            }

            is EngineState.Paused -> {
                val effectiveStartedAt = backDate(now, current.segmentElapsed)
                val workoutStartedAt = backDate(now, current.totalElapsed)
                val segment = template.segments[current.currentIndex]
                mutableRecords += SegmentRecord(
                    segmentId = segment.id,
                    index = current.currentIndex,
                    type = segment.type,
                    startedAt = effectiveStartedAt,
                    endedAt = now,
                    pausedDuration = 0.0,
                    measurements = liveMeasurements,
                    stationDisplayName = segment.stationKind?.displayName,
                    plannedDistanceMeters = segment.distanceMeters,
                )
                currentSegmentPausedDuration = 0.0
                liveMeasurements = SegmentMeasurements()
                state = EngineState.Finished(
                    workoutStartedAt = workoutStartedAt,
                    finishedAt = now,
                )
            }

            else -> throw EngineError.InvalidTransition(stateLabel, "finish")
        }
    }

    fun ingest(locationSample: LocationSample) {
        val segment = currentSegment ?: return
        if (state !is EngineState.Running || !segment.type.tracksLocation) return
        liveMeasurements = liveMeasurements.copy(
            locationSamples = liveMeasurements.locationSamples + locationSample,
        )
    }

    fun ingest(heartRateSample: HeartRateSample) {
        if (state !is EngineState.Running) return
        liveMeasurements = liveMeasurements.copy(
            heartRateSamples = liveMeasurements.heartRateSamples + heartRateSample,
        )
    }

    val liveMeasurementsSnapshot: SegmentMeasurements
        get() = liveMeasurements

    fun makeCompletedWorkout(): CompletedWorkout {
        val current = state as? EngineState.Finished
            ?: throw EngineError.InvalidTransition(stateLabel, "makeCompletedWorkout")
        return CompletedWorkout(
            templateName = template.name,
            division = template.division,
            startedAt = current.workoutStartedAt,
            finishedAt = current.finishedAt,
            segments = records,
        )
    }

    private val stateLabel: String
        get() = when (state) {
            EngineState.Idle -> "idle"
            is EngineState.Running -> "running"
            is EngineState.Paused -> "paused"
            is EngineState.Finished -> "finished"
        }

    private fun appendCurrentRecord(
        index: Int,
        segmentStartedAt: Instant,
        endedAt: Instant,
    ) {
        val segment = template.segments[index]
        mutableRecords += SegmentRecord(
            segmentId = segment.id,
            index = index,
            type = segment.type,
            startedAt = segmentStartedAt,
            endedAt = endedAt,
            pausedDuration = currentSegmentPausedDuration,
            measurements = liveMeasurements,
            stationDisplayName = segment.stationKind?.displayName,
            plannedDistanceMeters = segment.distanceMeters,
        )
    }

    private fun secondsBetween(start: Instant, end: Instant): Double =
        ChronoUnit.MILLIS.between(start, end).toDouble() / 1000.0

    private fun backDate(now: Instant, elapsedSeconds: Double): Instant =
        now.minusMillis((elapsedSeconds * 1000.0).roundToLong())
}
