package com.bbdyno.hyroxsim.feature.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import com.bbdyno.hyroxsim.core.domain.StationKind
import com.bbdyno.hyroxsim.core.domain.WorkoutSegment
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate
import com.bbdyno.hyroxsim.core.persistence.repository.TemplateRepository
import com.bbdyno.hyroxsim.sync.garmin.GarminTemplateSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BuilderUiState(
    val name: String = "My HYROX Workout",
    val division: HyroxDivision = HyroxDivision.MenOpenSingle,
    val usesRoxZone: Boolean = true,
    /** Logical segments (Run + Station pairs) — ROX Zones are materialised at save time. */
    val logicalSegments: List<WorkoutSegment> = defaultLogical(HyroxDivision.MenOpenSingle),
    val saving: Boolean = false,
    val savedMessage: String? = null,
) {
    companion object {
        fun defaultLogical(division: HyroxDivision): List<WorkoutSegment> =
            WorkoutTemplate.hyroxPreset(division).logicalSegments
    }
}

/**
 * ViewModel for the custom workout builder. Supports:
 * - Division switching (resets logical segments to division's preset)
 * - ROX Zone toggle (materialised at save time)
 * - Per-segment operations: add/delete/reorder
 *
 * Segment editing is scoped to the *logical* list (run + stations without
 * ROX zones). ROX zones are re-inserted automatically on save via
 * `WorkoutTemplate.materialize` when `usesRoxZone=true`.
 */
@HiltViewModel
class BuilderViewModel @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val garminSync: GarminTemplateSyncService,
) : ViewModel() {

    private val _ui = MutableStateFlow(BuilderUiState())
    val ui = _ui.asStateFlow()

    val savedTemplates: Flow<List<WorkoutTemplate>> = templateRepository.observeAll()

    fun onNameChanged(name: String) {
        _ui.value = _ui.value.copy(name = name)
    }

    fun onDivisionChanged(division: HyroxDivision) {
        _ui.value = _ui.value.copy(
            division = division,
            logicalSegments = BuilderUiState.defaultLogical(division),
        )
    }

    fun onRoxZoneToggled(enabled: Boolean) {
        _ui.value = _ui.value.copy(usesRoxZone = enabled)
    }

    fun onAddRun() {
        val state = _ui.value
        _ui.value = state.copy(
            logicalSegments = state.logicalSegments + WorkoutSegment.run(1000.0),
        )
    }

    fun onAddStation(kind: StationKind = StationKind.SkiErg) {
        val state = _ui.value
        _ui.value = state.copy(
            logicalSegments = state.logicalSegments + WorkoutSegment.station(kind),
        )
    }

    fun onDeleteSegment(index: Int) {
        val state = _ui.value
        if (index !in state.logicalSegments.indices) return
        _ui.value = state.copy(
            logicalSegments = state.logicalSegments.filterIndexed { i, _ -> i != index },
        )
    }

    fun onMoveUp(index: Int) {
        val state = _ui.value
        if (index <= 0 || index >= state.logicalSegments.size) return
        val mutable = state.logicalSegments.toMutableList()
        val tmp = mutable[index - 1]
        mutable[index - 1] = mutable[index]
        mutable[index] = tmp
        _ui.value = state.copy(logicalSegments = mutable)
    }

    fun onMoveDown(index: Int) {
        val state = _ui.value
        if (index < 0 || index >= state.logicalSegments.lastIndex) return
        val mutable = state.logicalSegments.toMutableList()
        val tmp = mutable[index + 1]
        mutable[index + 1] = mutable[index]
        mutable[index] = tmp
        _ui.value = state.copy(logicalSegments = mutable)
    }

    fun onResetToPreset() {
        val state = _ui.value
        _ui.value = state.copy(
            logicalSegments = BuilderUiState.defaultLogical(state.division),
        )
    }

    fun onSave() {
        if (_ui.value.saving) return
        val state = _ui.value
        _ui.value = state.copy(saving = true, savedMessage = null)
        viewModelScope.launch {
            val segments = WorkoutTemplate.materialize(
                logicalSegments = state.logicalSegments,
                usesRoxZone = state.usesRoxZone,
            )
            val template = WorkoutTemplate(
                id = java.util.UUID.randomUUID().toString(),
                name = state.name.ifBlank { "HYROX ${state.division.displayName}" },
                division = state.division,
                segments = segments,
                usesRoxZone = state.usesRoxZone,
                isBuiltIn = false,
            )
            runCatching {
                templateRepository.save(template)
                garminSync.push(template)
            }.onSuccess {
                _ui.value = state.copy(
                    saving = false,
                    savedMessage = "저장 완료 (가민 동기화 시도)",
                )
            }.onFailure { err ->
                _ui.value = state.copy(
                    saving = false,
                    savedMessage = "저장 실패: ${err.message}",
                )
            }
        }
    }

    fun clearMessage() {
        _ui.value = _ui.value.copy(savedMessage = null)
    }

    fun delete(template: WorkoutTemplate) {
        viewModelScope.launch {
            runCatching {
                templateRepository.delete(template.id)
                garminSync.delete(template.id)
            }
        }
    }
}
