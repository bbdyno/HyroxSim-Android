package com.bbdyno.hyroxsim.feature.goalsetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbdyno.hyroxsim.core.domain.PaceReference
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate
import com.bbdyno.hyroxsim.core.persistence.repository.Goal
import com.bbdyno.hyroxsim.core.persistence.repository.GoalRepository
import com.bbdyno.hyroxsim.core.persistence.repository.TemplateRepository
import com.bbdyno.hyroxsim.sync.garmin.GarminGoalSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalSetupUiState(
    val template: WorkoutTemplate? = null,
    val totalSeconds: Int = 75 * 60,
    val perSegmentSeconds: List<Int> = emptyList(),
    val saving: Boolean = false,
    val message: String? = null,
)

/**
 * Lets the user set a target total time for a template. The per-segment
 * breakdown uses PaceReference's proportional split so it updates live
 * as the slider moves. On Save: persists to Room and pushes to the
 * Garmin watch so the delta badge is live from the first tick.
 */
@HiltViewModel
class GoalSetupViewModel @Inject constructor(
    private val templates: TemplateRepository,
    private val goals: GoalRepository,
    private val garminSync: GarminGoalSyncService,
) : ViewModel() {

    private val _ui = MutableStateFlow(GoalSetupUiState())
    val ui: StateFlow<GoalSetupUiState> = _ui.asStateFlow()

    fun load(templateId: String) {
        viewModelScope.launch {
            val template = templates.findById(templateId) ?: return@launch
            val existing = goals.find(templateId)
            val totalSec = (existing?.targetTotalMs?.div(1000)?.toInt())
                ?: template.division?.let(PaceReference::referenceTotalSeconds)
                ?: (80 * 60)
            _ui.value = GoalSetupUiState(
                template = template,
                totalSeconds = totalSec,
                perSegmentSeconds = PaceReference.defaultTargetSegmentsSeconds(template, totalSec),
            )
        }
    }

    fun onTotalChanged(newTotalSec: Int) {
        val template = _ui.value.template ?: return
        _ui.value = _ui.value.copy(
            totalSeconds = newTotalSec,
            perSegmentSeconds = PaceReference.defaultTargetSegmentsSeconds(template, newTotalSec),
        )
    }

    fun onSave(onDone: () -> Unit) {
        val state = _ui.value
        val template = state.template ?: return
        viewModelScope.launch {
            _ui.value = state.copy(saving = true, message = null)
            runCatching {
                val perSegMs = state.perSegmentSeconds.map { it.toLong() * 1000 }
                goals.save(
                    Goal(
                        templateId = template.id,
                        targetTotalMs = state.totalSeconds.toLong() * 1000,
                        targetSegmentsMs = perSegMs,
                    )
                )
                template.division?.let { division ->
                    garminSync.sendGoal(
                        division = division,
                        templateName = template.name,
                        targetTotalMs = state.totalSeconds.toLong() * 1000,
                        targetSegmentsMs = perSegMs,
                    )
                }
            }.onSuccess {
                _ui.value = _ui.value.copy(saving = false, message = "Saved")
                onDone()
            }.onFailure { err ->
                _ui.value = _ui.value.copy(saving = false, message = "Save failed: ${err.message}")
            }
        }
    }
}
