package com.bbdyno.hyroxsim.core.domain

/**
 * A single station spec within a HYROX division.
 */
data class HyroxStationSpec(
    val kind: StationKind,
    val target: StationTarget,
    val weightKg: Double? = null,
    val weightNote: String? = null,
)

/**
 * Station specs per division, in official order. Ported 1:1 from iOS
 * `HyroxDivisionSpec.swift:35-106`. Verify against current rulebook.
 *
 * MixedDouble reuses MenOpen specs — HYROX has not published a canonical
 * mixed division spec; iOS uses the same approximation.
 */
object HyroxDivisionSpec {
    fun stationsFor(division: HyroxDivision): List<HyroxStationSpec> = when (division) {
        HyroxDivision.MenOpenSingle, HyroxDivision.MenOpenDouble, HyroxDivision.MixedDouble ->
            menOpen
        HyroxDivision.MenProSingle, HyroxDivision.MenProDouble -> menPro
        HyroxDivision.WomenOpenSingle, HyroxDivision.WomenOpenDouble -> womenOpen
        HyroxDivision.WomenProSingle, HyroxDivision.WomenProDouble -> womenPro
    }

    private val menOpen = listOf(
        HyroxStationSpec(StationKind.SkiErg, StationTarget.Distance(1000)),
        HyroxStationSpec(StationKind.SledPush, StationTarget.Distance(50), 152.0, "sled total"),
        HyroxStationSpec(StationKind.SledPull, StationTarget.Distance(50), 103.0, "sled total"),
        HyroxStationSpec(StationKind.BurpeeBroadJumps, StationTarget.Distance(80)),
        HyroxStationSpec(StationKind.Rowing, StationTarget.Distance(1000)),
        HyroxStationSpec(StationKind.FarmersCarry, StationTarget.Distance(200), 24.0, "per hand"),
        HyroxStationSpec(StationKind.SandbagLunges, StationTarget.Distance(100), 20.0),
        HyroxStationSpec(StationKind.WallBalls, StationTarget.Reps(100), 6.0),
    )

    private val menPro = listOf(
        HyroxStationSpec(StationKind.SkiErg, StationTarget.Distance(1000)),
        HyroxStationSpec(StationKind.SledPush, StationTarget.Distance(50), 202.0, "sled total"),
        HyroxStationSpec(StationKind.SledPull, StationTarget.Distance(50), 153.0, "sled total"),
        HyroxStationSpec(StationKind.BurpeeBroadJumps, StationTarget.Distance(80)),
        HyroxStationSpec(StationKind.Rowing, StationTarget.Distance(1000)),
        HyroxStationSpec(StationKind.FarmersCarry, StationTarget.Distance(200), 32.0, "per hand"),
        HyroxStationSpec(StationKind.SandbagLunges, StationTarget.Distance(100), 30.0),
        HyroxStationSpec(StationKind.WallBalls, StationTarget.Reps(100), 9.0),
    )

    private val womenOpen = listOf(
        HyroxStationSpec(StationKind.SkiErg, StationTarget.Distance(1000)),
        HyroxStationSpec(StationKind.SledPush, StationTarget.Distance(50), 102.0, "sled total"),
        HyroxStationSpec(StationKind.SledPull, StationTarget.Distance(50), 78.0, "sled total"),
        HyroxStationSpec(StationKind.BurpeeBroadJumps, StationTarget.Distance(80)),
        HyroxStationSpec(StationKind.Rowing, StationTarget.Distance(1000)),
        HyroxStationSpec(StationKind.FarmersCarry, StationTarget.Distance(200), 16.0, "per hand"),
        HyroxStationSpec(StationKind.SandbagLunges, StationTarget.Distance(100), 10.0),
        HyroxStationSpec(StationKind.WallBalls, StationTarget.Reps(75), 4.0),
    )

    private val womenPro = listOf(
        HyroxStationSpec(StationKind.SkiErg, StationTarget.Distance(1000)),
        HyroxStationSpec(StationKind.SledPush, StationTarget.Distance(50), 152.0, "sled total"),
        HyroxStationSpec(StationKind.SledPull, StationTarget.Distance(50), 103.0, "sled total"),
        HyroxStationSpec(StationKind.BurpeeBroadJumps, StationTarget.Distance(80)),
        HyroxStationSpec(StationKind.Rowing, StationTarget.Distance(1000)),
        HyroxStationSpec(StationKind.FarmersCarry, StationTarget.Distance(200), 24.0, "per hand"),
        HyroxStationSpec(StationKind.SandbagLunges, StationTarget.Distance(100), 20.0),
        HyroxStationSpec(StationKind.WallBalls, StationTarget.Reps(100), 6.0),
    )
}
