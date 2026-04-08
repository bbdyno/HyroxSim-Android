//
//  HeartRateZone.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable

enum class HeartRateZone(
    val label: String,
    val description: String,
    val rangeStart: Double,
    val rangeEnd: Double,
) : Serializable {
    Z1("Z1", "Very Light", 0.50, 0.60),
    Z2("Z2", "Light", 0.60, 0.70),
    Z3("Z3", "Moderate", 0.70, 0.80),
    Z4("Z4", "Hard", 0.80, 0.90),
    Z5("Z5", "Maximum", 0.90, 1.00),
    ;

    companion object {
        fun zoneForHeartRate(bpm: Int, maxHeartRate: Int): HeartRateZone {
            if (maxHeartRate <= 0) return Z1
            val percentage = bpm.toDouble() / maxHeartRate.toDouble()
            return when {
                percentage < 0.60 -> Z1
                percentage < 0.70 -> Z2
                percentage < 0.80 -> Z3
                percentage < 0.90 -> Z4
                else -> Z5
            }
        }
    }
}
