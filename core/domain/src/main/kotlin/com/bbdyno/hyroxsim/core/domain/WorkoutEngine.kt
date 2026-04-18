package com.bbdyno.hyroxsim.core.domain

/**
 * Pure state machine managing workout progression. Mirrors iOS
 * `HyroxCore.WorkoutEngine` and Monkey C `WorkoutEngine.mc`.
 *
 * Time handling: all public methods take an epoch-millisecond `nowMs`
 * parameter. The engine never reads the system clock internally — this
 * keeps it deterministic and easy to unit-test.
 *
 * Memory: `maxHeartRateSamples` caps the live buffer to avoid unbounded
 * growth during multi-hour workouts. Default 3600 samples = 1 hour at 1 Hz.
 */
class WorkoutEngine(
    val template: WorkoutTemplate,
    private val maxHeartRateSamples: Int = 3600,
) {
    var state: EngineState = EngineState.Idle
        private set

    private val _records: MutableList<SegmentRecord> = mutableListOf()
    val records: List<SegmentRecord> get() = _records.toList()

    private var currentSegmentPausedMs: Long = 0L
    private var liveLocationSamples = mutableListOf<LocationSample>()
    private var liveHeartRateSamples = mutableListOf<HeartRateSample>()

    // MARK: - Queries

    val currentSegmentIndex: Int?
        get() = when (val s = state) {
            is EngineState.Running -> s.currentIndex
            is EngineState.Paused -> s.currentIndex
            else -> null
        }

    val currentSegment: WorkoutSegment?
        get() = currentSegmentIndex?.let { template.segments.getOrNull(it) }

    val nextSegment: WorkoutSegment?
        get() = currentSegmentIndex?.let { idx ->
            template.segments.getOrNull(idx + 1)
        }

    val isLastSegment: Boolean
        get() = currentSegmentIndex?.let { it == template.segments.lastIndex } ?: false

    val isFinished: Boolean get() = state is EngineState.Finished

    fun segmentElapsedMs(nowMs: Long): Long = when (val s = state) {
        is EngineState.Running -> nowMs - s.segmentStartedAtEpochMs
        is EngineState.Paused -> s.segmentElapsedMs
        else -> 0L
    }

    fun totalElapsedMs(nowMs: Long): Long = when (val s = state) {
        is EngineState.Running -> nowMs - s.workoutStartedAtEpochMs
        is EngineState.Paused -> s.totalElapsedMs
        is EngineState.Finished -> s.finishedAtEpochMs - s.workoutStartedAtEpochMs
        else -> 0L
    }

    val liveMeasurementsSnapshot: SegmentMeasurements
        get() = SegmentMeasurements(
            locationSamples = liveLocationSamples.toList(),
            heartRateSamples = liveHeartRateSamples.toList(),
        )

    // MARK: - Actions

    fun start(nowMs: Long) {
        if (state !is EngineState.Idle) {
            throw EngineError.InvalidTransition(state.label, "start")
        }
        if (template.segments.isEmpty()) throw EngineError.EmptyTemplate
        resetLiveBuffers()
        state = EngineState.Running(0, nowMs, nowMs)
    }

    fun advance(nowMs: Long) {
        val running = state as? EngineState.Running
            ?: throw EngineError.InvalidTransition(state.label, "advance")
        flushRecord(
            index = running.currentIndex,
            segStartMs = running.segmentStartedAtEpochMs,
            endedAtMs = nowMs,
            pausedMs = currentSegmentPausedMs,
        )
        val nextIdx = running.currentIndex + 1
        resetLiveBuffers()
        state = if (nextIdx < template.segments.size) {
            EngineState.Running(nextIdx, nowMs, running.workoutStartedAtEpochMs)
        } else {
            EngineState.Finished(running.workoutStartedAtEpochMs, nowMs)
        }
    }

    fun pause(nowMs: Long) {
        val running = state as? EngineState.Running
            ?: throw EngineError.InvalidTransition(state.label, "pause")
        state = EngineState.Paused(
            currentIndex = running.currentIndex,
            segmentElapsedMs = nowMs - running.segmentStartedAtEpochMs,
            totalElapsedMs = nowMs - running.workoutStartedAtEpochMs,
        )
    }

    fun resume(nowMs: Long) {
        val paused = state as? EngineState.Paused
            ?: throw EngineError.InvalidTransition(state.label, "resume")
        currentSegmentPausedMs = 0L
        state = EngineState.Running(
            currentIndex = paused.currentIndex,
            segmentStartedAtEpochMs = nowMs - paused.segmentElapsedMs,
            workoutStartedAtEpochMs = nowMs - paused.totalElapsedMs,
        )
    }

    fun finish(nowMs: Long) {
        when (val s = state) {
            is EngineState.Running -> {
                flushRecord(s.currentIndex, s.segmentStartedAtEpochMs, nowMs, currentSegmentPausedMs)
                resetLiveBuffers()
                state = EngineState.Finished(s.workoutStartedAtEpochMs, nowMs)
            }
            is EngineState.Paused -> {
                val effectiveStart = nowMs - s.segmentElapsedMs
                val wkStart = nowMs - s.totalElapsedMs
                flushRecord(s.currentIndex, effectiveStart, nowMs, 0L)
                resetLiveBuffers()
                state = EngineState.Finished(wkStart, nowMs)
            }
            else -> throw EngineError.InvalidTransition(state.label, "finish")
        }
    }

    fun undo(@Suppress("UNUSED_PARAMETER") nowMs: Long) {
        val wkStart = when (val s = state) {
            is EngineState.Running -> s.workoutStartedAtEpochMs
            is EngineState.Finished -> s.workoutStartedAtEpochMs
            else -> throw EngineError.InvalidTransition(state.label, "undo")
        }
        if (_records.isEmpty()) throw EngineError.NothingToUndo
        val last = _records.removeAt(_records.lastIndex)
        resetLiveBuffers()
        state = EngineState.Running(
            currentIndex = last.index,
            segmentStartedAtEpochMs = last.startedAtEpochMs,
            workoutStartedAtEpochMs = wkStart,
        )
    }

    // MARK: - Sample ingest

    fun ingestHeartRate(timestampEpochMs: Long, bpm: Int) {
        if (state !is EngineState.Running) return
        if (liveHeartRateSamples.size >= maxHeartRateSamples) return
        liveHeartRateSamples.add(HeartRateSample(timestampEpochMs, bpm))
    }

    fun ingestLocation(sample: LocationSample) {
        if (state !is EngineState.Running) return
        val seg = currentSegment ?: return
        if (!seg.type.tracksLocation) return
        liveLocationSamples.add(sample)
    }

    // MARK: - Completion

    fun makeCompletedWorkout(source: WorkoutSource): CompletedWorkout {
        val finished = state as? EngineState.Finished
            ?: throw EngineError.InvalidTransition(state.label, "makeCompletedWorkout")
        return CompletedWorkout(
            templateName = template.name,
            division = template.division,
            startedAtEpochMs = finished.workoutStartedAtEpochMs,
            finishedAtEpochMs = finished.finishedAtEpochMs,
            source = source,
            segments = records,
        )
    }

    // MARK: - Private

    private fun flushRecord(
        index: Int,
        segStartMs: Long,
        endedAtMs: Long,
        pausedMs: Long,
    ) {
        val segment = template.segments[index]
        val record = SegmentRecord(
            segmentId = segment.id,
            index = index,
            type = segment.type,
            startedAtEpochMs = segStartMs,
            endedAtEpochMs = endedAtMs,
            pausedDurationMs = pausedMs,
            measurements = liveMeasurementsSnapshot,
            stationDisplayName = segment.stationKind?.displayName,
            plannedDistanceMeters = segment.distanceMeters,
            goalDurationSeconds = segment.goalDurationSeconds,
        )
        _records.add(record)
    }

    private fun resetLiveBuffers() {
        currentSegmentPausedMs = 0L
        liveLocationSamples = mutableListOf()
        liveHeartRateSamples = mutableListOf()
    }
}
