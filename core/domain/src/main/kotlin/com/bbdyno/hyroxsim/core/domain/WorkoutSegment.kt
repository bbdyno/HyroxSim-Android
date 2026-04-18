package com.bbdyno.hyroxsim.core.domain

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class WorkoutSegment(
    val id: String = UUID.randomUUID().toString(),
    val type: SegmentType,
    val distanceMeters: Double? = null,
    val goalDurationSeconds: Double? = null,
    val stationKind: StationKind? = null,
    val stationTarget: StationTarget? = null,
    val weightKg: Double? = null,
    val weightNote: String? = null,
) {
    companion object {
        fun run(distanceMeters: Double = 1000.0): WorkoutSegment = WorkoutSegment(
            type = SegmentType.Run,
            distanceMeters = distanceMeters,
            goalDurationSeconds = defaultGoal(SegmentType.Run, distanceMeters),
        )

        fun roxZone(): WorkoutSegment = WorkoutSegment(
            type = SegmentType.RoxZone,
            goalDurationSeconds = defaultGoal(SegmentType.RoxZone, null),
        )

        fun station(
            kind: StationKind,
            target: StationTarget? = null,
            weightKg: Double? = null,
            weightNote: String? = null,
        ): WorkoutSegment = WorkoutSegment(
            type = SegmentType.Station,
            goalDurationSeconds = defaultGoal(SegmentType.Station, null),
            stationKind = kind,
            stationTarget = target ?: kind.defaultTarget,
            weightKg = weightKg,
            weightNote = weightNote,
        )

        fun defaultGoal(type: SegmentType, distanceMeters: Double?): Double = when (type) {
            SegmentType.Run -> (distanceMeters ?: 1000.0) * 0.36
            SegmentType.RoxZone -> 30.0
            SegmentType.Station -> 240.0
        }
    }
}
