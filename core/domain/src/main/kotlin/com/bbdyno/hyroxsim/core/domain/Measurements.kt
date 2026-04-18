package com.bbdyno.hyroxsim.core.domain

import kotlinx.serialization.Serializable
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Serializable
data class HeartRateSample(
    val timestampEpochMs: Long,
    val bpm: Int,
)

@Serializable
data class LocationSample(
    val timestampEpochMs: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val horizontalAccuracy: Double? = null,
    val speed: Double? = null,
)

/**
 * Buffers raw sensor samples for an in-progress segment. Attached to a
 * completed segment record when the engine advances.
 */
@Serializable
data class SegmentMeasurements(
    val locationSamples: List<LocationSample> = emptyList(),
    val heartRateSamples: List<HeartRateSample> = emptyList(),
) {
    val averageHeartRate: Int?
        get() = if (heartRateSamples.isEmpty()) null
                else heartRateSamples.sumOf { it.bpm } / heartRateSamples.size

    val maxHeartRate: Int?
        get() = heartRateSamples.maxOfOrNull { it.bpm }

    /** Cumulative GPS distance via Haversine; skips samples with >30m accuracy error. */
    val distanceMeters: Double
        get() {
            if (locationSamples.size < 2) return 0.0
            var total = 0.0
            for (i in 1 until locationSamples.size) {
                val prev = locationSamples[i - 1]
                val curr = locationSamples[i]
                if ((prev.horizontalAccuracy ?: 0.0) > 30.0) continue
                if ((curr.horizontalAccuracy ?: 0.0) > 30.0) continue
                total += haversine(prev, curr)
            }
            return total
        }

    fun averagePaceSecondsPerKm(activeDurationSeconds: Double): Double? {
        val km = distanceMeters / 1000.0
        return if (km > 0.001) activeDurationSeconds / km else null
    }

    private fun haversine(a: LocationSample, b: LocationSample): Double {
        val R = 6_371_000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val h = sin(dLat / 2).pow2() + cos(lat1) * cos(lat2) * sin(dLon / 2).pow2()
        return R * 2 * asin(sqrt(h))
    }

    private fun Double.pow2(): Double = this * this
}
