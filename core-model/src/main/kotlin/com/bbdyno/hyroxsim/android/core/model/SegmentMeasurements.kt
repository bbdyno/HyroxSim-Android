//
//  SegmentMeasurements.kt
//  core-model
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.model

import java.io.Serializable
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class SegmentMeasurements(
    val locationSamples: List<LocationSample> = emptyList(),
    val heartRateSamples: List<HeartRateSample> = emptyList(),
) : Serializable {

    val distanceMeters: Double
        get() {
            val filtered = locationSamples.filter { it.horizontalAccuracy <= 30.0 }
            if (filtered.size < 2) return 0.0

            var total = 0.0
            for (index in 1 until filtered.size) {
                total += haversineDistance(filtered[index - 1], filtered[index])
            }
            return total
        }

    fun averagePaceSecondsPerKm(activeDuration: Double): Double? {
        val kilometers = distanceMeters / 1000.0
        if (kilometers <= 0.001) return null
        return activeDuration / kilometers
    }

    val averageHeartRate: Int?
        get() = if (heartRateSamples.isEmpty()) {
            null
        } else {
            heartRateSamples.sumOf { it.bpm } / heartRateSamples.size
        }

    val maxHeartRate: Int?
        get() = heartRateSamples.maxOfOrNull { it.bpm }

    val minHeartRate: Int?
        get() = heartRateSamples.minOfOrNull { it.bpm }
}

private fun haversineDistance(a: LocationSample, b: LocationSample): Double {
    val earthRadius = 6_371_000.0
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val deltaLat = Math.toRadians(b.latitude - a.latitude)
    val deltaLon = Math.toRadians(b.longitude - a.longitude)

    val h = sin(deltaLat / 2) * sin(deltaLat / 2) +
        cos(lat1) * cos(lat2) * sin(deltaLon / 2) * sin(deltaLon / 2)
    val c = 2 * atan2(sqrt(h), sqrt(1 - h))

    return earthRadius * c
}
