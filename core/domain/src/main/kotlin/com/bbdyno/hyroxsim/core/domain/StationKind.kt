package com.bbdyno.hyroxsim.core.domain

import kotlinx.serialization.Serializable

/**
 * Exercise station kind. Standard 8 + custom.
 *
 * Interop: iOS uses an enum with associated value for `custom(name)`;
 * on this side we serialize as raw string "skiErg" etc. with a separate
 * name field carried at the segment level for custom stations.
 */
@Serializable
enum class StationKind(val raw: String) {
    SkiErg("skiErg"),
    SledPush("sledPush"),
    SledPull("sledPull"),
    BurpeeBroadJumps("burpeeBroadJumps"),
    Rowing("rowing"),
    FarmersCarry("farmersCarry"),
    SandbagLunges("sandbagLunges"),
    WallBalls("wallBalls"),
    Custom("custom");

    val displayName: String
        get() = when (this) {
            SkiErg -> "SkiErg"
            SledPush -> "Sled Push"
            SledPull -> "Sled Pull"
            BurpeeBroadJumps -> "Burpee Broad Jumps"
            Rowing -> "Rowing"
            FarmersCarry -> "Farmers Carry"
            SandbagLunges -> "Sandbag Lunges"
            WallBalls -> "Wall Balls"
            Custom -> "Custom"
        }

    val defaultTarget: StationTarget
        get() = when (this) {
            SkiErg, Rowing -> StationTarget.Distance(meters = 1000)
            SledPush, SledPull -> StationTarget.Distance(meters = 50)
            BurpeeBroadJumps -> StationTarget.Distance(meters = 80)
            FarmersCarry -> StationTarget.Distance(meters = 200)
            SandbagLunges -> StationTarget.Distance(meters = 100)
            WallBalls -> StationTarget.Reps(count = 100)
            Custom -> StationTarget.None
        }

    companion object {
        val standardOrder = listOf(
            SkiErg, SledPush, SledPull, BurpeeBroadJumps,
            Rowing, FarmersCarry, SandbagLunges, WallBalls
        )

        fun fromRaw(raw: String): StationKind? = entries.firstOrNull { it.raw == raw }
    }
}
