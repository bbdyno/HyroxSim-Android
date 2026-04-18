package com.bbdyno.hyroxsim.feature.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbdyno.hyroxsim.core.domain.EngineState
import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import com.bbdyno.hyroxsim.core.domain.WorkoutEngine
import com.bbdyno.hyroxsim.core.domain.WorkoutSource
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate
import com.bbdyno.hyroxsim.core.persistence.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ActiveWorkoutUiState(
    val engineStateLabel: String = "idle",
    val currentSegmentIndex: Int = 0,
    val totalSegments: Int = 0,
    val segmentLabel: String = "",
    val nextLabel: String? = null,
    val segmentElapsedMs: Long = 0,
    val totalElapsedMs: Long = 0,
    val finished: Boolean = false,
)

/**
 * Single-workout view model. Hosts the [WorkoutEngine] and drives the UI
 * state flow. On finish, persists the result via [WorkoutRepository]
 * (source = manual since we don't yet support in-app Android sensors;
 * Garmin imports go through a separate service).
 */
@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
) : ViewModel() {

    private var engine: WorkoutEngine? = null
    private val _ui = MutableStateFlow(ActiveWorkoutUiState())
    val ui: StateFlow<ActiveWorkoutUiState> = _ui.asStateFlow()

    fun startForDivision(divisionRaw: String) {
        val division = HyroxDivision.fromRaw(divisionRaw) ?: return
        val template = WorkoutTemplate.hyroxPreset(division)
        val eng = WorkoutEngine(template)
        engine = eng
        eng.start(nowMs())
        viewModelScope.launch { tickLoop() }
    }

    private suspend fun tickLoop() {
        while (engine?.isFinished != true) {
            refresh()
            delay(500)
        }
        refresh()
    }

    private fun refresh() {
        val eng = engine ?: return
        val now = nowMs()
        val seg = eng.currentSegment
        val next = eng.nextSegment
        val idx = eng.currentSegmentIndex ?: -1
        _ui.value = ActiveWorkoutUiState(
            engineStateLabel = eng.state.label,
            currentSegmentIndex = idx,
            totalSegments = eng.template.segments.size,
            segmentLabel = seg?.let(::labelFor) ?: "",
            nextLabel = next?.let(::labelFor),
            segmentElapsedMs = eng.segmentElapsedMs(now),
            totalElapsedMs = eng.totalElapsedMs(now),
            finished = eng.isFinished,
        )
    }

    fun onAdvance() {
        val eng = engine ?: return
        if (eng.state !is EngineState.Running) return
        eng.advance(nowMs()); refresh()
    }

    fun onPause() { engine?.pause(nowMs()); refresh() }
    fun onResume() { engine?.resume(nowMs()); refresh() }

    fun onEnd(onPersisted: () -> Unit) {
        val eng = engine ?: return
        eng.finish(nowMs())
        viewModelScope.launch {
            runCatching {
                repository.save(eng.makeCompletedWorkout(source = WorkoutSource.Manual))
            }
            refresh()
            onPersisted()
        }
    }

    private fun labelFor(segment: com.bbdyno.hyroxsim.core.domain.WorkoutSegment): String =
        when (segment.type) {
            com.bbdyno.hyroxsim.core.domain.SegmentType.Run -> "Run"
            com.bbdyno.hyroxsim.core.domain.SegmentType.RoxZone -> "ROX Zone"
            com.bbdyno.hyroxsim.core.domain.SegmentType.Station ->
                segment.stationKind?.displayName ?: "Station"
        }

    private fun nowMs(): Long = Instant.now().toEpochMilli()
}
