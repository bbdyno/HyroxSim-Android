//
//  ExerciseSessionManager.kt
//  data-healthservices
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.healthservices

interface ExerciseSessionManager {
    var exerciseListener: ExerciseSessionListener?
    var availabilityListener: ExerciseAvailabilityListener?

    fun refreshCapabilities(
        onSuccess: (WorkoutTrackingCapabilities) -> Unit,
        onError: (Throwable) -> Unit = {},
    )

    fun prepareExercise(
        warmUpHeartRate: Boolean,
        warmUpLocation: Boolean,
        listener: ResultListener = ResultListener {},
    )

    fun startExercise(
        useGps: Boolean,
        listener: ResultListener = ResultListener {},
    )

    fun pauseExercise(listener: ResultListener = ResultListener {})

    fun resumeExercise(listener: ResultListener = ResultListener {})

    fun endExercise(listener: ResultListener = ResultListener {})
}

interface HeartRateMonitor {
    var heartRateListener: HeartRateListener?
    var availabilityListener: AvailabilityListener?

    fun refreshSupport(
        onSuccess: (Boolean) -> Unit,
        onError: (Throwable) -> Unit = {},
    )

    fun start(listener: ResultListener = ResultListener {})

    fun stop(listener: ResultListener = ResultListener {})
}
