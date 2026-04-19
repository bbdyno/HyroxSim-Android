package com.bbdyno.hyroxsim.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import com.bbdyno.hyroxsim.core.domain.SegmentType
import com.bbdyno.hyroxsim.core.domain.WorkoutSegment
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate
import com.bbdyno.hyroxsim.core.persistence.repository.GoalRepository
import com.bbdyno.hyroxsim.core.persistence.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Resolves a saved template id or a built-in preset route key (`builtin:<division>`)
 * into a [WorkoutTemplate] for the detail screen. Also surfaces the stored
 * goal (if any) so the GOAL card can show a preview.
 *
 * `preservedRoxZones` keeps the user's original ROX segments around so
 * toggling rox off and back on doesn't lose their goal times.
 */
data class TemplateDetailUiState(
    val template: WorkoutTemplate? = null,
    val goalTotalSeconds: Int? = null,
    val preservedRoxZones: List<WorkoutSegment> = emptyList(),
)

@HiltViewModel
class TemplateDetailViewModel @Inject constructor(
    private val templates: TemplateRepository,
    private val goals: GoalRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(TemplateDetailUiState())
    val ui: StateFlow<TemplateDetailUiState> = _ui.asStateFlow()

    fun load(routeKey: String) {
        viewModelScope.launch {
            val template = templates.findById(routeKey)
                ?: HyroxDivision.fromRaw(routeKey.removePrefix(BUILTIN_PREFIX))?.let {
                    WorkoutTemplate.hyroxPreset(it)
                }
                ?: return@launch
            val goal = goals.find(template.id)
            _ui.value = TemplateDetailUiState(
                template = template,
                goalTotalSeconds = goal?.targetTotalMs?.div(1000)?.toInt(),
                preservedRoxZones = template.segments.filter { it.type == SegmentType.RoxZone },
            )
        }
    }

    /**
     * Toggle ROX Zone on/off. Re-materializes the segment list using
     * preserved rox segments so goal times aren't lost across toggles.
     * Saved (non-builtin) templates are persisted; built-in presets stay
     * in memory and flow to the Active Workout via navigation args.
     */
    fun onRoxZoneToggled(enabled: Boolean) {
        val state = _ui.value
        val template = state.template ?: return

        val logical = template.segments.filter { it.type != SegmentType.RoxZone }
        val preserved = if (enabled) state.preservedRoxZones else emptyList()
        val newSegments = WorkoutTemplate.materialize(
            logicalSegments = logical,
            usesRoxZone = enabled,
            preservedRoxZones = preserved,
        )
        val updated = template.copy(segments = newSegments, usesRoxZone = enabled)
        _ui.value = state.copy(
            template = updated,
            preservedRoxZones = if (enabled) state.preservedRoxZones
                else template.segments.filter { it.type == SegmentType.RoxZone }
                    .ifEmpty { state.preservedRoxZones },
        )
        if (!template.isBuiltIn) {
            viewModelScope.launch { templates.save(updated) }
        }
    }

    companion object {
        const val BUILTIN_PREFIX = "builtin:"
    }
}
