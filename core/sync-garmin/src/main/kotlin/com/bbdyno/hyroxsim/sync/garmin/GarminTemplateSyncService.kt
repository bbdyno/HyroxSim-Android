package com.bbdyno.hyroxsim.sync.garmin

import com.bbdyno.hyroxsim.core.domain.StationKind
import com.bbdyno.hyroxsim.core.domain.StationTarget
import com.bbdyno.hyroxsim.core.domain.WorkoutSegment
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate

/**
 * Pushes user-built [WorkoutTemplate]s (including `usesRoxZone=false`
 * variants) to the Garmin watch. Upsert is idempotent by template id;
 * delete removes a single template by id.
 *
 * Call on each template save/update. The watch stores the payload in
 * `TemplateStore` and surfaces it in the home Menu2.
 */
class GarminTemplateSyncService(private val bridge: GarminBridge) {

    fun push(template: WorkoutTemplate) {
        val payload = encodePayload(template)
        bridge.sendEnvelope(
            GarminMessageCodec.makeEnvelope(
                type = MessageProtocol.Type.TEMPLATE_UPSERT,
                payload = payload,
            )
        )
    }

    fun pushAll(templates: List<WorkoutTemplate>) = templates.forEach(::push)

    fun delete(id: String) {
        bridge.sendEnvelope(
            GarminMessageCodec.makeEnvelope(
                type = MessageProtocol.Type.TEMPLATE_DELETE,
                payload = mapOf("id" to id),
            )
        )
    }

    private fun encodePayload(template: WorkoutTemplate): Map<String, Any?> = mapOf(
        "id" to template.id,
        "name" to template.name,
        "division" to template.division?.raw,
        "segments" to template.segments.map(::encodeSegment),
        "usesRoxZone" to template.usesRoxZone,
        "createdAtMs" to template.createdAtEpochMs,
        "isBuiltIn" to template.isBuiltIn,
    )

    private fun encodeSegment(segment: WorkoutSegment): Map<String, Any?> = mapOf(
        "id" to segment.id,
        "type" to segment.type.raw,
        "distanceMeters" to segment.distanceMeters,
        "goalDurationSeconds" to segment.goalDurationSeconds,
        "stationKind" to segment.stationKind?.raw,
        "stationTarget" to segment.stationTarget?.let(::encodeTarget),
        "weightKg" to segment.weightKg,
        "weightNote" to segment.weightNote,
    )

    private fun encodeTarget(target: StationTarget): Map<String, Any?> = when (target) {
        is StationTarget.Distance -> mapOf("kind" to "distance", "meters" to target.meters)
        is StationTarget.Reps -> mapOf("kind" to "reps", "count" to target.count)
        is StationTarget.Duration -> mapOf("kind" to "duration", "seconds" to target.seconds)
        StationTarget.None -> mapOf("kind" to "none")
    }
}
