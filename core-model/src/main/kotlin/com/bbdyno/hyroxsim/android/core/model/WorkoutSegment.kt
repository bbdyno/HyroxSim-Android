//
//  WorkoutSegment.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable
import java.util.UUID

data class WorkoutSegment(
    val id: UUID = UUID.randomUUID(),
    val type: SegmentType,
    val distanceMeters: Double? = null,
    val stationKind: StationKind? = null,
    val stationTarget: StationTarget? = null,
    val weightKg: Double? = null,
    val weightNote: String? = null,
) : Serializable {

    fun validate() {
        when (type) {
            SegmentType.RUN, SegmentType.ROX_ZONE -> {
                if (stationKind != null || weightKg != null) {
                    throw WorkoutSegmentValidationException(
                        WorkoutSegmentValidationException.Kind.RunSegmentHasStationData,
                    )
                }
            }

            SegmentType.STATION -> {
                if (distanceMeters != null) {
                    throw WorkoutSegmentValidationException(
                        WorkoutSegmentValidationException.Kind.StationSegmentHasDistanceData,
                    )
                }
            }
        }
    }

    companion object {
        fun run(distanceMeters: Double = 1000.0): WorkoutSegment =
            WorkoutSegment(type = SegmentType.RUN, distanceMeters = distanceMeters)

        fun roxZone(): WorkoutSegment =
            WorkoutSegment(type = SegmentType.ROX_ZONE)

        fun station(
            kind: StationKind,
            target: StationTarget? = null,
            weightKg: Double? = null,
            weightNote: String? = null,
        ): WorkoutSegment =
            WorkoutSegment(
                type = SegmentType.STATION,
                stationKind = kind,
                stationTarget = target,
                weightKg = weightKg,
                weightNote = weightNote,
            )
    }
}

class WorkoutSegmentValidationException(
    val kind: Kind,
) : IllegalArgumentException(kind.message), Serializable {
    enum class Kind(val message: String) {
        RunSegmentHasStationData("Run/RoxZone segment should not have station data (stationKind, weightKg)"),
        StationSegmentHasDistanceData("Station segment should not have distanceMeters"),
    }
}
