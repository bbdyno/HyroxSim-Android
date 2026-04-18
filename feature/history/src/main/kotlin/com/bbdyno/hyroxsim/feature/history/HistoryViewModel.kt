package com.bbdyno.hyroxsim.feature.history

import androidx.lifecycle.ViewModel
import com.bbdyno.hyroxsim.core.persistence.repository.WorkoutRepository
import com.bbdyno.hyroxsim.core.persistence.repository.WorkoutSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: WorkoutRepository,
) : ViewModel() {
    val summaries: Flow<List<WorkoutSummary>> = repository.observeAllSummaries()
}
