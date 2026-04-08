//
//  EngineError.kt
//  core-engine
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.engine

import java.io.Serializable

sealed class EngineError(
    message: String,
) : IllegalStateException(message), Serializable {
    data class InvalidTransition(
        val from: String,
        val action: String,
    ) : EngineError("Invalid transition from $from using action $action")

    data object EmptyTemplate : EngineError("Workout template has no segments")

    data object NothingToUndo : EngineError("There are no completed segments to undo")
}
