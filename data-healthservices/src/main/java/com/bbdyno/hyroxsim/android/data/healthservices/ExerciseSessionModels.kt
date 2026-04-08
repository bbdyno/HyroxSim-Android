//
//  ExerciseSessionModels.kt
//  data-healthservices
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.healthservices

import com.bbdyno.hyroxsim.android.core.model.LocationSample
import java.time.Instant

data class WorkoutTrackingCapabilities(
    val supportsExerciseTracking: Boolean,
    val supportsHeartRate: Boolean,
    val supportsLocation: Boolean,
    val supportsHeartRateBatching: Boolean,
)

data class ExerciseAvailabilitySnapshot(
    val dataTypeName: String,
    val status: String,
)

data class ExerciseSessionSnapshot(
    val stateLabel: String,
    val activeDurationSeconds: Double,
    val totalDistanceMeters: Double?,
    val latestHeartRateBpm: Int?,
    val latestLocation: LocationSample?,
    val startTime: Instant?,
)

fun interface ExerciseSessionListener {
    fun onExerciseUpdate(snapshot: ExerciseSessionSnapshot)
}

fun interface ExerciseAvailabilityListener {
    fun onAvailabilityChanged(snapshot: ExerciseAvailabilitySnapshot)
}

fun interface HeartRateListener {
    fun onHeartRate(bpm: Int, measuredAt: Instant)
}

fun interface AvailabilityListener {
    fun onAvailability(status: String)
}

fun interface ResultListener {
    fun onResult(result: Result<Unit>)
}
