//
//  EngineState.kt
//  core-engine
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.engine

import java.io.Serializable
import java.time.Instant

sealed interface EngineState : Serializable {
    data object Idle : EngineState

    data class Running(
        val currentIndex: Int,
        val segmentStartedAt: Instant,
        val workoutStartedAt: Instant,
    ) : EngineState

    data class Paused(
        val currentIndex: Int,
        val segmentElapsed: Double,
        val totalElapsed: Double,
    ) : EngineState

    data class Finished(
        val workoutStartedAt: Instant,
        val finishedAt: Instant,
    ) : EngineState
}
