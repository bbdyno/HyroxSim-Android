package com.bbdyno.hyroxsim.feature.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbdyno.hyroxsim.core.domain.EngineState
import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import com.bbdyno.hyroxsim.core.domain.PaceReference
import com.bbdyno.hyroxsim.core.domain.SegmentType
import com.bbdyno.hyroxsim.core.domain.WorkoutEngine
import com.bbdyno.hyroxsim.core.domain.WorkoutSegment
import com.bbdyno.hyroxsim.core.domain.WorkoutSource
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate
import com.bbdyno.hyroxsim.core.persistence.repository.GoalRepository
import com.bbdyno.hyroxsim.core.persistence.repository.TemplateRepository
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
    val segmentTargetMs: Long = 0,
    val totalTargetMs: Long = 0,
    val segmentDeltaMs: Long = 0,
    val totalDeltaMs: Long = 0,
    val finished: Boolean = false,
    val savedWorkoutId: String? = null,
)

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val templates: TemplateRepository,
    private val goals: GoalRepository,
) : ViewModel() {

    private var engine: WorkoutEngine? = null
    private var perSegmentTargetsMs: List<Long> = emptyList()
    private var totalTargetMs: Long = 0

    private val _ui = MutableStateFlow(ActiveWorkoutUiState())
    val ui: StateFlow<ActiveWorkoutUiState> = _ui.asStateFlow()

    fun startForDivision(divisionRaw: String) {
        val division = HyroxDivision.fromRaw(divisionRaw) ?: return
        val template = WorkoutTemplate.hyroxPreset(division)
        resolveGoalAndStart(template)
    }

    fun startForTemplate(templateId: String) {
        viewModelScope.launch {
            val template = templates.findById(templateId) ?: return@launch
            resolveGoalAndStart(template)
        }
    }

    private fun resolveGoalAndStart(template: WorkoutTemplate) {
        viewModelScope.launch {
            val goal = goals.find(template.id)
            val targetsSec = goal?.targetSegmentsMs?.map { (it / 1000).toInt() }
                ?: PaceReference.defaultTargetSegmentsSeconds(template)
            perSegmentTargetsMs = targetsSec.map { it.toLong() * 1000 }
            totalTargetMs = goal?.targetTotalMs ?: perSegmentTargetsMs.sum()
            val eng = WorkoutEngine(template)
            engine = eng
            eng.start(nowMs())
            viewModelScope.launch { tickLoop() }
        }
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
        val idx = eng.currentSegmentIndex ?: -1
        val segElapsed = eng.segmentElapsedMs(now)
        val totElapsed = eng.totalElapsedMs(now)
        val segTarget = perSegmentTargetsMs.getOrNull(idx.coerceAtLeast(0)) ?: 0
        val cumulativeTarget = if (idx >= 0) {
            perSegmentTargetsMs.take(idx + 1).sum()
        } else {
            totalTargetMs
        }
        _ui.value = ActiveWorkoutUiState(
            engineStateLabel = eng.state.label,
            currentSegmentIndex = idx,
            totalSegments = eng.template.segments.size,
            segmentLabel = eng.currentSegment?.let(::labelFor) ?: "",
            nextLabel = eng.nextSegment?.let(::labelFor),
            segmentElapsedMs = segElapsed,
            totalElapsedMs = totElapsed,
            segmentTargetMs = segTarget,
            totalTargetMs = totalTargetMs,
            segmentDeltaMs = segElapsed - segTarget,
            totalDeltaMs = totElapsed - cumulativeTarget,
            finished = eng.isFinished,
            savedWorkoutId = _ui.value.savedWorkoutId,
        )
    }

    fun onAdvance() {
        val eng = engine ?: return
        if (eng.state !is EngineState.Running) return
        eng.advance(nowMs()); refresh()
    }

    fun onPause() { engine?.pause(nowMs()); refresh() }
    fun onResume() { engine?.resume(nowMs()); refresh() }

    fun onEnd(onPersisted: (String) -> Unit) {
        val eng = engine ?: return
        eng.finish(nowMs())
        viewModelScope.launch {
            val completed = eng.makeCompletedWorkout(source = WorkoutSource.Manual)
            runCatching { repository.save(completed) }
            refresh()
            _ui.value = _ui.value.copy(savedWorkoutId = completed.id)
            onPersisted(completed.id)
        }
    }

    private fun labelFor(segment: WorkoutSegment): String = when (segment.type) {
        SegmentType.Run -> "Run"
        SegmentType.RoxZone -> "ROX Zone"
        SegmentType.Station -> segment.stationKind?.displayName ?: "Station"
    }

    private fun nowMs(): Long = Instant.now().toEpochMilli()
}
