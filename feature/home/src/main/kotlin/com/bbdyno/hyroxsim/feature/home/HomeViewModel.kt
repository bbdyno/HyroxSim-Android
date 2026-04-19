package com.bbdyno.hyroxsim.feature.home

import androidx.lifecycle.ViewModel
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate
import com.bbdyno.hyroxsim.core.persistence.repository.TemplateRepository
import com.bbdyno.hyroxsim.core.persistence.repository.WorkoutRepository
import com.bbdyno.hyroxsim.core.persistence.repository.WorkoutSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class HomeUiState(
    val mostRecent: WorkoutSummary?,
    val savedTemplates: List<WorkoutTemplate>,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workouts: WorkoutRepository,
    private val templates: TemplateRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val ui: Flow<HomeUiState> = kotlinx.coroutines.flow.combine(
        workouts.observeAllSummaries(),
        templates.observeAll(),
    ) { summaries, tpls ->
        HomeUiState(
            mostRecent = summaries.firstOrNull(),
            savedTemplates = tpls,
        )
    }
}
