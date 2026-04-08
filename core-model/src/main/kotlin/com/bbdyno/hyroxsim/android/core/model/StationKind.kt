//
//  StationKind.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable

sealed interface StationKind : Serializable {
    val displayName: String
    val defaultTarget: StationTarget

    data object SkiErg : StationKind {
        override val displayName: String = "SkiErg"
        override val defaultTarget: StationTarget = StationTarget.distance(1000.0)
    }

    data object SledPush : StationKind {
        override val displayName: String = "Sled Push"
        override val defaultTarget: StationTarget = StationTarget.distance(50.0)
    }

    data object SledPull : StationKind {
        override val displayName: String = "Sled Pull"
        override val defaultTarget: StationTarget = StationTarget.distance(50.0)
    }

    data object BurpeeBroadJumps : StationKind {
        override val displayName: String = "Burpee Broad Jumps"
        override val defaultTarget: StationTarget = StationTarget.distance(80.0)
    }

    data object Rowing : StationKind {
        override val displayName: String = "Rowing"
        override val defaultTarget: StationTarget = StationTarget.distance(1000.0)
    }

    data object FarmersCarry : StationKind {
        override val displayName: String = "Farmers Carry"
        override val defaultTarget: StationTarget = StationTarget.distance(200.0)
    }

    data object SandbagLunges : StationKind {
        override val displayName: String = "Sandbag Lunges"
        override val defaultTarget: StationTarget = StationTarget.distance(100.0)
    }

    data object WallBalls : StationKind {
        override val displayName: String = "Wall Balls"
        override val defaultTarget: StationTarget = StationTarget.reps(100)
    }

    data class Custom(override val displayName: String) : StationKind {
        override val defaultTarget: StationTarget = StationTarget.none()
    }
}
