package com.bbdyno.hyroxsim.sync.garmin

import com.bbdyno.hyroxsim.core.domain.CompletedWorkout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Decodes `workout.completed` envelopes from the watch and forwards the
 * domain [CompletedWorkout] to whichever repository the caller hands in.
 * Transport-agnostic for unit testing.
 */
class GarminImportService(
    private val bridge: GarminBridge,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val onImport: suspend (CompletedWorkout) -> Unit,
) {
    fun start() {
        bridge.setOnMessageReceived { envelope -> handle(envelope) }
    }

    fun handle(envelope: Map<String, Any?>) {
        val type = envelope[MessageProtocol.Key.TYPE] as? String ?: return
        if (type != MessageProtocol.Type.WORKOUT_COMPLETED) return
        val workout = GarminMessageCodec.decodeWorkoutCompleted(envelope) ?: run {
            println("⚠️ GarminImportService: malformed workout.completed envelope")
            return
        }
        val id = envelope[MessageProtocol.Key.ID] as? String
        scope.launch {
            runCatching { onImport(workout) }
                .onSuccess { ackIfPossible(id) }
                .onFailure { println("⚠️ GarminImportService: persist failed $it") }
        }
    }

    private fun ackIfPossible(id: String?) {
        if (id == null) return
        bridge.sendEnvelope(
            GarminMessageCodec.makeEnvelope(
                type = MessageProtocol.Type.ACK,
                id = id,
            )
        )
    }
}
