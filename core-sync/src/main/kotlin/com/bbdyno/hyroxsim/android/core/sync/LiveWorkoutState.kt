//
//  LiveWorkoutState.kt
//  core-sync
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.sync

import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import java.io.Serializable
import java.time.Instant
import java.util.UUID

enum class WorkoutOrigin : Serializable {
    PHONE,
    WATCH,
}

data class HeartRateRelay(
    val bpm: Int,
    val timestamp: Instant = Instant.now(),
) : Serializable

data class LiveWorkoutState(
    val segmentLabel: String,
    val segmentSubLabel: String?,
    val segmentElapsedText: String,
    val totalElapsedText: String,
    val paceText: String,
    val distanceText: String,
    val heartRateText: String,
    val heartRateZoneRaw: Int?,
    val stationNameText: String?,
    val stationTargetText: String?,
    val accentKindRaw: String,
    val isPaused: Boolean,
    val isFinished: Boolean,
    val isLastSegment: Boolean,
    val gpsStrong: Boolean,
    val gpsActive: Boolean,
    val templateName: String,
    val totalSegmentCount: Int,
    val currentSegmentIndex: Int,
    val origin: WorkoutOrigin = WorkoutOrigin.WATCH,
) : Serializable

enum class WorkoutCommand : Serializable {
    ADVANCE,
    PAUSE,
    RESUME,
    END,
}

object LiveSyncKeys {
    const val liveState: String = "liveWorkoutState"
    const val command: String = "workoutCommand"
    const val workoutStarted: String = "workoutStarted"
    const val workoutFinished: String = "workoutFinished"
    const val templateData: String = "templateData"
    const val workoutOrigin: String = "workoutOrigin"
    const val heartRateRelay: String = "heartRateRelay"
}

interface SyncCoordinator {
    val isSupported: Boolean
    val isPaired: Boolean
    val isReachable: Boolean

    fun activate()
    fun sendTemplate(template: WorkoutTemplate)
    fun sendCompletedWorkout(workout: CompletedWorkout)
    fun sendTemplateDeleted(id: UUID)
    var onReceiveTemplate: ((WorkoutTemplate) -> Unit)?
    var onReceiveCompletedWorkout: ((CompletedWorkout) -> Unit)?
    var onReceiveTemplateDeleted: ((UUID) -> Unit)?
    fun sendWorkoutStarted(template: WorkoutTemplate, origin: WorkoutOrigin)
    fun sendLiveState(state: LiveWorkoutState)
    fun sendWorkoutFinished(origin: WorkoutOrigin)
    fun sendCommand(command: WorkoutCommand)
    fun sendHeartRateRelay(relay: HeartRateRelay)
    var onWorkoutStarted: ((WorkoutTemplate, WorkoutOrigin) -> Unit)?
    var onLiveStateReceived: ((LiveWorkoutState) -> Unit)?
    var onWorkoutFinished: ((WorkoutOrigin) -> Unit)?
    var onReceiveCommand: ((WorkoutCommand) -> Unit)?
    var onHeartRateRelayReceived: ((HeartRateRelay) -> Unit)?
    var onReachabilityChanged: ((Boolean) -> Unit)?
}
