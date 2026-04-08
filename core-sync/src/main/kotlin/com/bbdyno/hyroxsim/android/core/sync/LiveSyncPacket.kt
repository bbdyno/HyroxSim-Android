//
//  LiveSyncPacket.kt
//  core-sync
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.sync

import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.UUID

sealed interface LiveSyncPacket : Serializable {
    data class WorkoutStarted(
        val template: WorkoutTemplate,
        val origin: WorkoutOrigin,
    ) : LiveSyncPacket

    data class LiveStatePacket(
        val state: LiveWorkoutState,
    ) : LiveSyncPacket

    data class WorkoutFinished(
        val origin: WorkoutOrigin,
    ) : LiveSyncPacket

    data class CommandPacket(
        val command: WorkoutCommand,
    ) : LiveSyncPacket

    data class HeartRateRelayPacket(
        val relay: HeartRateRelay,
    ) : LiveSyncPacket
}

object LiveSyncPacketCoder {
    fun encode(packet: LiveSyncPacket): ByteArray = serialize(packet)

    fun decode(data: ByteArray): LiveSyncPacket =
        deserialize(data) as? LiveSyncPacket ?: throw SyncError.DecodingFailed
}

enum class SyncMessageKind : Serializable {
    TEMPLATE,
    COMPLETED_WORKOUT,
    TEMPLATE_DELETED,
}

data class SyncEnvelope(
    val kind: SyncMessageKind,
    val payload: ByteArray,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
) : Serializable

object SyncEnvelopeCoder {
    fun encodeTemplate(template: WorkoutTemplate): SyncEnvelope =
        SyncEnvelope(
            kind = SyncMessageKind.TEMPLATE,
            payload = serialize(template),
        )

    fun encodeCompletedWorkout(workout: CompletedWorkout): SyncEnvelope =
        SyncEnvelope(
            kind = SyncMessageKind.COMPLETED_WORKOUT,
            payload = serialize(workout),
        )

    fun encodeDeletedId(id: UUID): SyncEnvelope =
        SyncEnvelope(
            kind = SyncMessageKind.TEMPLATE_DELETED,
            payload = serialize(id),
        )

    fun decodeTemplate(envelope: SyncEnvelope): WorkoutTemplate =
        deserialize(envelope.payload) as? WorkoutTemplate ?: throw SyncError.DecodingFailed

    fun decodeCompletedWorkout(envelope: SyncEnvelope): CompletedWorkout =
        deserialize(envelope.payload) as? CompletedWorkout ?: throw SyncError.DecodingFailed

    fun decodeDeletedId(envelope: SyncEnvelope): UUID =
        deserialize(envelope.payload) as? UUID ?: throw SyncError.DecodingFailed
}

private fun serialize(value: Serializable): ByteArray =
    ByteArrayOutputStream().use { output ->
        ObjectOutputStream(output).use { stream ->
            stream.writeObject(value)
        }
        output.toByteArray()
    }

private fun deserialize(bytes: ByteArray): Any =
    ByteArrayInputStream(bytes).use { input ->
        ObjectInputStream(input).use { stream ->
            stream.readObject()
        }
    }
