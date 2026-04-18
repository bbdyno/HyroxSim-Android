package com.bbdyno.hyroxsim.sync.garmin

import com.bbdyno.hyroxsim.core.domain.CompletedWorkout
import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import com.bbdyno.hyroxsim.core.domain.SegmentMeasurements
import com.bbdyno.hyroxsim.core.domain.SegmentRecord
import com.bbdyno.hyroxsim.core.domain.SegmentType
import com.bbdyno.hyroxsim.core.domain.WorkoutSource
import java.util.UUID

/**
 * Encodes and decodes the v1 envelope/payload pairs exchanged with the
 * Garmin Connect IQ watch app. Pure value-typed glue — does not touch
 * the ConnectIQ AAR so this module builds even when the AAR is absent.
 */
object GarminMessageCodec {

    fun makeEnvelope(
        type: String,
        id: String = UUID.randomUUID().toString(),
        payload: Map<String, Any?>? = null,
    ): Map<String, Any?> = buildMap {
        put(MessageProtocol.Key.VERSION, MessageProtocol.VERSION)
        put(MessageProtocol.Key.TYPE, type)
        put(MessageProtocol.Key.ID, id)
        if (payload != null) put(MessageProtocol.Key.PAYLOAD, payload)
    }

    fun encodeGoalSet(
        division: HyroxDivision,
        templateName: String,
        targetTotalMs: Long,
        targetSegmentsMs: List<Long>,
    ): Map<String, Any?> = makeEnvelope(
        type = MessageProtocol.Type.GOAL_SET,
        payload = mapOf(
            "division" to division.raw,
            "templateName" to templateName,
            "targetTotalMs" to targetTotalMs,
            "targetSegmentsMs" to targetSegmentsMs,
        ),
    )

    /** Returns null when the envelope is malformed. Callers surface a soft error. */
    fun decodeWorkoutCompleted(envelope: Map<String, Any?>): CompletedWorkout? {
        if (envelope[MessageProtocol.Key.TYPE] != MessageProtocol.Type.WORKOUT_COMPLETED) return null
        val payload = envelope[MessageProtocol.Key.PAYLOAD] as? Map<*, *> ?: return null

        val id = (payload["id"] as? String) ?: UUID.randomUUID().toString()
        val templateName = payload["templateName"] as? String ?: return null
        val startedAtMs = (payload["startedAtMs"] as? Number)?.toLong() ?: return null
        val finishedAtMs = (payload["finishedAtMs"] as? Number)?.toLong() ?: return null
        val division = (payload["division"] as? String)?.let(HyroxDivision::fromRaw)
        val sourceRaw = payload["source"] as? String
        val source = sourceRaw?.let(WorkoutSource::fromRaw) ?: WorkoutSource.Garmin

        val rawSegments = payload["segments"] as? List<*> ?: emptyList<Any>()
        val segments = rawSegments
            .mapNotNull { it as? Map<*, *> }
            .mapNotNull(::decodeSegment)

        return CompletedWorkout(
            id = id,
            templateName = templateName,
            division = division,
            startedAtEpochMs = startedAtMs,
            finishedAtEpochMs = finishedAtMs,
            source = source,
            segments = segments,
        )
    }

    private fun decodeSegment(dict: Map<*, *>): SegmentRecord? {
        val index = (dict["index"] as? Number)?.toInt() ?: return null
        val type = (dict["type"] as? String)?.let(SegmentType::fromRaw) ?: return null
        val startedAtMs = (dict["startedAtMs"] as? Number)?.toLong() ?: return null
        val endedAtMs = (dict["endedAtMs"] as? Number)?.toLong() ?: return null
        val pausedMs = (dict["pausedDurationMs"] as? Number)?.toLong() ?: 0L
        val hrRaw = dict["heartRateSamples"] as? List<*> ?: emptyList<Any>()
        val hr = hrRaw.mapNotNull { e ->
            val m = e as? Map<*, *> ?: return@mapNotNull null
            val t = (m["tMs"] as? Number)?.toLong() ?: return@mapNotNull null
            val bpm = (m["bpm"] as? Number)?.toInt() ?: return@mapNotNull null
            com.bbdyno.hyroxsim.core.domain.HeartRateSample(t, bpm)
        }
        return SegmentRecord(
            segmentId = UUID.randomUUID().toString(),
            index = index,
            type = type,
            startedAtEpochMs = startedAtMs,
            endedAtEpochMs = endedAtMs,
            pausedDurationMs = pausedMs,
            measurements = SegmentMeasurements(heartRateSamples = hr),
            stationDisplayName = dict["stationDisplayName"] as? String,
            plannedDistanceMeters = (dict["plannedDistanceMeters"] as? Number)?.toDouble(),
            goalDurationSeconds = (dict["goalDurationSeconds"] as? Number)?.toDouble(),
        )
    }
}
