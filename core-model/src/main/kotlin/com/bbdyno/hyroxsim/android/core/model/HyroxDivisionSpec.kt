//
//  HyroxDivisionSpec.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable

data class HyroxStationSpec(
    val kind: StationKind,
    val target: StationTarget,
    val weightKg: Double? = null,
    val weightNote: String? = null,
) : Serializable

object HyroxDivisionSpec {
    fun stations(division: HyroxDivision): List<HyroxStationSpec> =
        when (division) {
            HyroxDivision.MEN_OPEN_SINGLE,
            HyroxDivision.MEN_OPEN_DOUBLE,
            -> menOpenSpecs

            HyroxDivision.MEN_PRO_SINGLE,
            HyroxDivision.MEN_PRO_DOUBLE,
            -> menProSpecs

            HyroxDivision.WOMEN_OPEN_SINGLE,
            HyroxDivision.WOMEN_OPEN_DOUBLE,
            -> womenOpenSpecs

            HyroxDivision.WOMEN_PRO_SINGLE,
            HyroxDivision.WOMEN_PRO_DOUBLE,
            -> womenProSpecs

            HyroxDivision.MIXED_DOUBLE -> menOpenSpecs
        }

    private val menOpenSpecs = listOf(
        HyroxStationSpec(StationKind.SkiErg, StationTarget.distance(1000.0)),
        HyroxStationSpec(StationKind.SledPush, StationTarget.distance(50.0), weightKg = 152.0, weightNote = "sled total"),
        HyroxStationSpec(StationKind.SledPull, StationTarget.distance(50.0), weightKg = 103.0, weightNote = "sled total"),
        HyroxStationSpec(StationKind.BurpeeBroadJumps, StationTarget.distance(80.0)),
        HyroxStationSpec(StationKind.Rowing, StationTarget.distance(1000.0)),
        HyroxStationSpec(StationKind.FarmersCarry, StationTarget.distance(200.0), weightKg = 24.0, weightNote = "per hand"),
        HyroxStationSpec(StationKind.SandbagLunges, StationTarget.distance(100.0), weightKg = 20.0),
        HyroxStationSpec(StationKind.WallBalls, StationTarget.reps(100), weightKg = 6.0),
    )

    private val menProSpecs = listOf(
        HyroxStationSpec(StationKind.SkiErg, StationTarget.distance(1000.0)),
        HyroxStationSpec(StationKind.SledPush, StationTarget.distance(50.0), weightKg = 202.0, weightNote = "sled total"),
        HyroxStationSpec(StationKind.SledPull, StationTarget.distance(50.0), weightKg = 153.0, weightNote = "sled total"),
        HyroxStationSpec(StationKind.BurpeeBroadJumps, StationTarget.distance(80.0)),
        HyroxStationSpec(StationKind.Rowing, StationTarget.distance(1000.0)),
        HyroxStationSpec(StationKind.FarmersCarry, StationTarget.distance(200.0), weightKg = 32.0, weightNote = "per hand"),
        HyroxStationSpec(StationKind.SandbagLunges, StationTarget.distance(100.0), weightKg = 30.0),
        HyroxStationSpec(StationKind.WallBalls, StationTarget.reps(100), weightKg = 9.0),
    )

    private val womenOpenSpecs = listOf(
        HyroxStationSpec(StationKind.SkiErg, StationTarget.distance(1000.0)),
        HyroxStationSpec(StationKind.SledPush, StationTarget.distance(50.0), weightKg = 102.0, weightNote = "sled total"),
        HyroxStationSpec(StationKind.SledPull, StationTarget.distance(50.0), weightKg = 78.0, weightNote = "sled total"),
        HyroxStationSpec(StationKind.BurpeeBroadJumps, StationTarget.distance(80.0)),
        HyroxStationSpec(StationKind.Rowing, StationTarget.distance(1000.0)),
        HyroxStationSpec(StationKind.FarmersCarry, StationTarget.distance(200.0), weightKg = 16.0, weightNote = "per hand"),
        HyroxStationSpec(StationKind.SandbagLunges, StationTarget.distance(100.0), weightKg = 10.0),
        HyroxStationSpec(StationKind.WallBalls, StationTarget.reps(75), weightKg = 4.0),
    )

    private val womenProSpecs = listOf(
        HyroxStationSpec(StationKind.SkiErg, StationTarget.distance(1000.0)),
        HyroxStationSpec(StationKind.SledPush, StationTarget.distance(50.0), weightKg = 152.0, weightNote = "sled total"),
        HyroxStationSpec(StationKind.SledPull, StationTarget.distance(50.0), weightKg = 103.0, weightNote = "sled total"),
        HyroxStationSpec(StationKind.BurpeeBroadJumps, StationTarget.distance(80.0)),
        HyroxStationSpec(StationKind.Rowing, StationTarget.distance(1000.0)),
        HyroxStationSpec(StationKind.FarmersCarry, StationTarget.distance(200.0), weightKg = 24.0, weightNote = "per hand"),
        HyroxStationSpec(StationKind.SandbagLunges, StationTarget.distance(100.0), weightKg = 20.0),
        HyroxStationSpec(StationKind.WallBalls, StationTarget.reps(100), weightKg = 6.0),
    )
}
