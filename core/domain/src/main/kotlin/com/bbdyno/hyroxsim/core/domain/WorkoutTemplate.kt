package com.bbdyno.hyroxsim.core.domain

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class WorkoutTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val division: HyroxDivision? = null,
    val segments: List<WorkoutSegment>,
    val usesRoxZone: Boolean = true,
    val createdAtEpochMs: Long = Instant.now().toEpochMilli(),
    val isBuiltIn: Boolean = false,
) {
    val totalRunDistanceMeters: Double
        get() = segments.filter { it.type == SegmentType.Run }
            .mapNotNull { it.distanceMeters }
            .sum()

    val stationCount: Int
        get() = segments.count { it.type == SegmentType.Station }

    val estimatedDurationSeconds: Double
        get() = segments.sumOf {
            it.goalDurationSeconds ?: WorkoutSegment.defaultGoal(it.type, it.distanceMeters)
        }

    val logicalSegments: List<WorkoutSegment>
        get() = segments.filter { it.type != SegmentType.RoxZone }

    companion object {
        /**
         * Canonical HYROX preset: 8×(Run + Station) with ROX Zone transitions
         * between every (run, station) and (station, run) pair. Totals 31 segments.
         */
        fun hyroxPreset(division: HyroxDivision): WorkoutTemplate {
            val stationSpecs = HyroxDivisionSpec.stationsFor(division)
            val logical = buildList {
                repeat(8) { i ->
                    add(WorkoutSegment.run(1000.0))
                    val spec = stationSpecs[i]
                    add(
                        WorkoutSegment.station(
                            kind = spec.kind,
                            target = spec.target,
                            weightKg = spec.weightKg,
                            weightNote = spec.weightNote,
                        )
                    )
                }
            }
            val materialized = materialize(logical, usesRoxZone = true)
            return WorkoutTemplate(
                name = "HYROX ${division.displayName}",
                division = division,
                segments = materialized,
                usesRoxZone = true,
                isBuiltIn = true,
            )
        }

        fun materialize(
            logicalSegments: List<WorkoutSegment>,
            usesRoxZone: Boolean,
            preservedRoxZones: List<WorkoutSegment> = emptyList(),
        ): List<WorkoutSegment> {
            if (!usesRoxZone) return logicalSegments
            val out = mutableListOf<WorkoutSegment>()
            var preservedIdx = 0
            logicalSegments.forEachIndexed { i, seg ->
                out.add(seg)
                if (i < logicalSegments.lastIndex && needsRoxZoneBetween(seg, logicalSegments[i + 1])) {
                    out.add(
                        preservedRoxZones.getOrNull(preservedIdx++) ?: WorkoutSegment.roxZone()
                    )
                }
            }
            return out
        }

        private fun needsRoxZoneBetween(a: WorkoutSegment, b: WorkoutSegment): Boolean = when {
            a.type == SegmentType.Run && b.type == SegmentType.Station -> true
            a.type == SegmentType.Station && b.type == SegmentType.Run -> true
            else -> false
        }
    }
}
