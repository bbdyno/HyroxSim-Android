package com.bbdyno.hyroxsim.core.domain

sealed class EngineError(message: String) : RuntimeException(message) {
    class InvalidTransition(from: String, action: String)
        : EngineError("cannot $action from state '$from'")

    data object EmptyTemplate : EngineError("template has no segments") {
        private fun readResolve(): Any = EmptyTemplate
    }

    data object NothingToUndo : EngineError("no completed segments to undo") {
        private fun readResolve(): Any = NothingToUndo
    }
}
