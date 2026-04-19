package com.bbdyno.hyroxsim.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate
import com.bbdyno.hyroxsim.core.persistence.repository.Goal
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
 */
data class TemplateDetailUiState(
    val template: WorkoutTemplate? = null,
    val goalTotalSeconds: Int? = null,
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
            )
        }
    }

    companion object {
        const val BUILTIN_PREFIX = "builtin:"
    }
}
