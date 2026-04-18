package com.bbdyno.hyroxsim.feature.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbdyno.hyroxsim.core.domain.HyroxDivision
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
    val saving: Boolean = false,
    val savedMessage: String? = null,
)

/**
 * ViewModel for the custom workout builder. MVP scope: pick division +
 * toggle ROX Zone. Segment-level editing (reps/weights/custom stations)
 * is TBD — mirrors iOS staged UX.
 *
 * On save: persist via [TemplateRepository] *and* push to the Garmin
 * watch via [GarminTemplateSyncService] so the watch's TemplateStore
 * receives the same payload.
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
        _ui.value = _ui.value.copy(division = division)
    }

    fun onRoxZoneToggled(enabled: Boolean) {
        _ui.value = _ui.value.copy(usesRoxZone = enabled)
    }

    fun onSave() {
        if (_ui.value.saving) return
        val state = _ui.value
        _ui.value = state.copy(saving = true, savedMessage = null)
        viewModelScope.launch {
            val preset = WorkoutTemplate.hyroxPreset(state.division)
            val segments = WorkoutTemplate.materialize(
                logicalSegments = preset.logicalSegments,
                usesRoxZone = state.usesRoxZone,
            )
            val template = preset.copy(
                id = java.util.UUID.randomUUID().toString(),
                name = state.name.ifBlank { "HYROX ${state.division.displayName}" },
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
