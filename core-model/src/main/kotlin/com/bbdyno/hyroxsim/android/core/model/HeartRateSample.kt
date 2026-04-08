//
//  HeartRateSample.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable
import java.time.Instant

data class HeartRateSample(
    val timestamp: Instant,
    val bpm: Int,
) : Serializable
