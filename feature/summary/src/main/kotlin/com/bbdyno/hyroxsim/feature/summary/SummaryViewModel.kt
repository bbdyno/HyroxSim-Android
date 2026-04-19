package com.bbdyno.hyroxsim.feature.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbdyno.hyroxsim.core.domain.CompletedWorkout
import com.bbdyno.hyroxsim.core.persistence.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SummaryUiState(
    val workout: CompletedWorkout? = null,
    val loading: Boolean = true,
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val repository: WorkoutRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(SummaryUiState())
    val ui: StateFlow<SummaryUiState> = _ui.asStateFlow()

    fun load(workoutId: String) {
        viewModelScope.launch {
            val w = repository.findById(workoutId)
            _ui.value = SummaryUiState(workout = w, loading = false)
        }
    }
}
