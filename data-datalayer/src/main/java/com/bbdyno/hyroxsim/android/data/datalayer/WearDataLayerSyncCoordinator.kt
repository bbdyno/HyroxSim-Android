//
//  WearDataLayerSyncCoordinator.kt
//  data-datalayer
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.datalayer

import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import com.bbdyno.hyroxsim.android.core.sync.HeartRateRelay
import com.bbdyno.hyroxsim.android.core.sync.LiveSyncPacket
import com.bbdyno.hyroxsim.android.core.sync.LiveSyncPacketCoder
import com.bbdyno.hyroxsim.android.core.sync.LiveWorkoutState
import com.bbdyno.hyroxsim.android.core.sync.SyncCoordinator
import com.bbdyno.hyroxsim.android.core.sync.SyncEnvelope
import com.bbdyno.hyroxsim.android.core.sync.SyncEnvelopeCoder
import com.bbdyno.hyroxsim.android.core.sync.SyncMessageKind
import com.bbdyno.hyroxsim.android.core.sync.WorkoutCommand
import com.bbdyno.hyroxsim.android.core.sync.WorkoutOrigin
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.UUID

class WearDataLayerSyncCoordinator(
    private val transport: DataLayerTransport,
) : SyncCoordinator, DataLayerEventListener {
    override var isSupported: Boolean = true
        private set
    override var isPaired: Boolean = false
        private set
    override var isReachable: Boolean = false
        private set

    override var onReceiveTemplate: ((WorkoutTemplate) -> Unit)? = null
    override var onReceiveCompletedWorkout: ((CompletedWorkout) -> Unit)? = null
    override var onReceiveTemplateDeleted: ((UUID) -> Unit)? = null
    override var onWorkoutStarted: ((WorkoutTemplate, WorkoutOrigin) -> Unit)? = null
    override var onLiveStateReceived: ((LiveWorkoutState) -> Unit)? = null
    override var onWorkoutFinished: ((WorkoutOrigin) -> Unit)? = null
    override var onReceiveCommand: ((WorkoutCommand) -> Unit)? = null
    override var onHeartRateRelayReceived: ((HeartRateRelay) -> Unit)? = null
    override var onReachabilityChanged: ((Boolean) -> Unit)? = null

    override fun activate() {
        transport.addListener(this)
        transport.refreshConnectedNodes(
            onSuccess = { nodes ->
                isPaired = nodes.isNotEmpty()
                isReachable = nodes.any { it.isNearby }
                onReachabilityChanged?.invoke(isReachable)
            },
            onError = {
                isPaired = false
                isReachable = false
                onReachabilityChanged?.invoke(false)
            },
        )
    }

    override fun sendTemplate(template: WorkoutTemplate) {
        val envelope = SyncEnvelopeCoder.encodeTemplate(template)
        transport.putData(
            path = "${HyroxDataLayerPaths.templatePrefix}/${template.id}",
            payload = serialize(envelope),
            urgent = false,
        )
    }

    override fun sendCompletedWorkout(workout: CompletedWorkout) {
        val envelope = SyncEnvelopeCoder.encodeCompletedWorkout(workout)
        transport.putData(
            path = "${HyroxDataLayerPaths.completedWorkoutPrefix}/${workout.id}",
            payload = serialize(envelope),
            urgent = false,
        )
    }

    override fun sendTemplateDeleted(id: UUID) {
        val envelope = SyncEnvelopeCoder.encodeDeletedId(id)
        transport.putData(
            path = "${HyroxDataLayerPaths.templateDeletedPrefix}/$id",
            payload = serialize(envelope),
            urgent = false,
        )
    }

    override fun sendWorkoutStarted(template: WorkoutTemplate, origin: WorkoutOrigin) {
        sendLivePacket(LiveSyncPacket.WorkoutStarted(template, origin))
    }

    override fun sendLiveState(state: LiveWorkoutState) {
        sendLivePacket(LiveSyncPacket.LiveStatePacket(state))
    }

    override fun sendWorkoutFinished(origin: WorkoutOrigin) {
        sendLivePacket(LiveSyncPacket.WorkoutFinished(origin))
    }

    override fun sendCommand(command: WorkoutCommand) {
        sendLivePacket(LiveSyncPacket.CommandPacket(command))
    }

    override fun sendHeartRateRelay(relay: HeartRateRelay) {
        sendLivePacket(LiveSyncPacket.HeartRateRelayPacket(relay))
    }

    override fun onEvent(event: DataLayerEvent) {
        when (event) {
            is DataLayerEvent.MessageReceived -> handleMessageEvent(event)
            is DataLayerEvent.DataItemChanged -> handleDataEvent(event)
        }
    }

    private fun sendLivePacket(packet: LiveSyncPacket) {
        transport.sendMessage(
            path = HyroxDataLayerPaths.livePacket,
            payload = LiveSyncPacketCoder.encode(packet),
            nearbyOnly = true,
        )
    }

    private fun handleMessageEvent(event: DataLayerEvent.MessageReceived) {
        if (event.path != HyroxDataLayerPaths.livePacket) return
        val packet = runCatching { LiveSyncPacketCoder.decode(event.payload) }.getOrNull() ?: return
        when (packet) {
            is LiveSyncPacket.WorkoutStarted -> onWorkoutStarted?.invoke(packet.template, packet.origin)
            is LiveSyncPacket.LiveStatePacket -> onLiveStateReceived?.invoke(packet.state)
            is LiveSyncPacket.WorkoutFinished -> onWorkoutFinished?.invoke(packet.origin)
            is LiveSyncPacket.CommandPacket -> onReceiveCommand?.invoke(packet.command)
            is LiveSyncPacket.HeartRateRelayPacket -> onHeartRateRelayReceived?.invoke(packet.relay)
        }
    }

    private fun handleDataEvent(event: DataLayerEvent.DataItemChanged) {
        val envelope = runCatching { deserialize(event.payload) as SyncEnvelope }.getOrNull() ?: return
        when (envelope.kind) {
            SyncMessageKind.TEMPLATE -> {
                runCatching { SyncEnvelopeCoder.decodeTemplate(envelope) }
                    .onSuccess { onReceiveTemplate?.invoke(it) }
            }

            SyncMessageKind.COMPLETED_WORKOUT -> {
                runCatching { SyncEnvelopeCoder.decodeCompletedWorkout(envelope) }
                    .onSuccess { onReceiveCompletedWorkout?.invoke(it) }
            }

            SyncMessageKind.TEMPLATE_DELETED -> {
                runCatching { SyncEnvelopeCoder.decodeDeletedId(envelope) }
                    .onSuccess { onReceiveTemplateDeleted?.invoke(it) }
            }
        }
    }

    private fun serialize(envelope: SyncEnvelope): ByteArray =
        ByteArrayOutputStream().use { output ->
            ObjectOutputStream(output).use { stream ->
                stream.writeObject(envelope)
            }
            output.toByteArray()
        }

    private fun deserialize(payload: ByteArray): Any =
        ByteArrayInputStream(payload).use { input ->
            ObjectInputStream(input).use { stream ->
                stream.readObject()
            }
        }
}
