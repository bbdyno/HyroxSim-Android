package com.bbdyno.hyroxsim.core.domain

sealed interface EngineState {
    data object Idle : EngineState

    data class Running(
        val currentIndex: Int,
        val segmentStartedAtEpochMs: Long,
        val workoutStartedAtEpochMs: Long,
    ) : EngineState

    data class Paused(
        val currentIndex: Int,
        val segmentElapsedMs: Long,
        val totalElapsedMs: Long,
    ) : EngineState

    data class Finished(
        val workoutStartedAtEpochMs: Long,
        val finishedAtEpochMs: Long,
    ) : EngineState

    val label: String
        get() = when (this) {
            is Idle -> "idle"
            is Running -> "running"
            is Paused -> "paused"
            is Finished -> "finished"
        }
}
