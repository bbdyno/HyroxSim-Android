//
//  MainActivity.kt
//  app-wear
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.wear

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.bbdyno.hyroxsim.android.core.engine.EngineState
import com.bbdyno.hyroxsim.android.core.engine.WorkoutEngine
import com.bbdyno.hyroxsim.android.core.format.DistanceFormatter
import com.bbdyno.hyroxsim.android.core.format.DurationFormatter
import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.core.model.HeartRateSample
import com.bbdyno.hyroxsim.android.core.model.SegmentType
import com.bbdyno.hyroxsim.android.core.model.WorkoutSegment
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import com.bbdyno.hyroxsim.android.core.sync.LiveWorkoutState
import com.bbdyno.hyroxsim.android.core.sync.WorkoutCommand
import com.bbdyno.hyroxsim.android.core.sync.WorkoutOrigin
import com.bbdyno.hyroxsim.android.data.datalayer.WearDataLayerSyncCoordinator
import com.bbdyno.hyroxsim.android.data.datalayer.WearOsDataLayerTransport
import com.bbdyno.hyroxsim.android.data.healthservices.ExerciseSessionListener
import com.bbdyno.hyroxsim.android.data.healthservices.HealthServicesExerciseSessionManager
import com.bbdyno.hyroxsim.android.data.healthservices.HealthServicesHeartRateMonitor
import com.bbdyno.hyroxsim.android.data.healthservices.HeartRateListener
import com.bbdyno.hyroxsim.android.data.local.LocalWorkoutLibrary
import com.bbdyno.hyroxsim.android.feature.active.wear.ActiveWearScreen
import com.bbdyno.hyroxsim.android.feature.history.wear.HistoryWearScreen
import com.bbdyno.hyroxsim.android.feature.home.wear.ConfirmStartWearScreen
import com.bbdyno.hyroxsim.android.feature.home.wear.HomeWearFeatureScreen
import com.bbdyno.hyroxsim.android.feature.summary.wear.SummaryWearScreen
import java.time.Instant
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val library = LocalWorkoutLibrary(applicationContext).also { it.seedIfEmpty() }
        val syncCoordinator = WearDataLayerSyncCoordinator(
            transport = WearOsDataLayerTransport(applicationContext),
        ).also { it.activate() }
        val exerciseManager = HealthServicesExerciseSessionManager(applicationContext)
        val heartRateMonitor = HealthServicesHeartRateMonitor(applicationContext)

        setContent {
            WearApp(
                library = library,
                syncCoordinator = syncCoordinator,
                exerciseManager = exerciseManager,
                heartRateMonitor = heartRateMonitor,
            )
        }
    }
}

private enum class WearDestination {
    HOME,
    CONFIRM_START,
    HISTORY,
    SUMMARY,
    ACTIVE_LOCAL,
    ACTIVE_MIRROR,
}

private data class WearSession(
    val template: WorkoutTemplate,
    val engine: WorkoutEngine,
    val origin: WorkoutOrigin,
)

@Composable
private fun WearApp(
    library: LocalWorkoutLibrary,
    syncCoordinator: WearDataLayerSyncCoordinator,
    exerciseManager: HealthServicesExerciseSessionManager,
    heartRateMonitor: HealthServicesHeartRateMonitor,
) {
    val context = LocalContext.current
    val templates = remember { mutableStateListOf<WorkoutTemplate>() }
    val history = remember { mutableStateListOf<CompletedWorkout>() }
    var destination by remember { mutableStateOf(WearDestination.HOME) }
    var selectedTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var selectedSummary by remember { mutableStateOf<CompletedWorkout?>(null) }
    var localSession by remember { mutableStateOf<WearSession?>(null) }
    var mirrorState by remember { mutableStateOf<LiveWorkoutState?>(null) }
    var isReachable by remember { mutableStateOf(syncCoordinator.isReachable) }
    var now by remember { mutableStateOf(Instant.now()) }
    var heartRateRelayActive by remember { mutableStateOf(false) }
    var pendingStartTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var pendingRelayPermissionRequest by remember { mutableStateOf(false) }
    var permissionNotice by remember { mutableStateOf<String?>(null) }
    var pendingPermissionRequest by remember { mutableStateOf<List<String>?>(null) }
    var startApproved by remember { mutableStateOf(false) }
    var relayApproved by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val allGranted = grants.values.all { it }

        if (allGranted) {
            if (pendingStartTemplate != null) {
                startApproved = true
            }
            if (pendingRelayPermissionRequest) {
                relayApproved = true
            }
        } else {
            permissionNotice = when {
                pendingStartTemplate != null ->
                    "Watch workouts need motion, heart rate, and location permissions."

                pendingRelayPermissionRequest ->
                    "Heart rate relay stays off until sensor permission is granted."

                else -> null
            }
            pendingStartTemplate = null
            pendingRelayPermissionRequest = false
        }
    }

    fun refreshTemplates() {
        templates.clear()
        templates.addAll(library.allTemplates())
    }

    fun refreshHistory() {
        history.clear()
        history.addAll(library.completedWorkouts())
    }

    fun currentLocalState(): LiveWorkoutState? {
        val session = localSession ?: return null
        return buildWearLiveState(
            template = session.template,
            engine = session.engine,
            now = now,
            origin = session.origin,
        )
    }

    fun stopRelayMonitor() {
        if (heartRateRelayActive) {
            heartRateMonitor.stop()
            heartRateRelayActive = false
        }
    }

    fun finishLocalSession(forceFinish: Boolean) {
        val session = localSession ?: return
        if (forceFinish && !session.engine.isFinished) {
            session.engine.finish(now)
        }
        if (!session.engine.isFinished) return

        exerciseManager.endExercise()
        stopRelayMonitor()

        val workout = session.engine.makeCompletedWorkout()
        library.saveCompletedWorkout(workout)
        refreshHistory()
        selectedSummary = workout
        localSession = null
        destination = WearDestination.SUMMARY

        if (session.origin == WorkoutOrigin.WATCH) {
            syncCoordinator.sendCompletedWorkout(workout)
            syncCoordinator.sendWorkoutFinished(WorkoutOrigin.WATCH)
        }
    }

    fun applyLocalCommand(command: WorkoutCommand) {
        val session = localSession ?: return
        if (session.origin != WorkoutOrigin.WATCH) return

        when (command) {
            WorkoutCommand.ADVANCE -> {
                session.engine.advance(now)
                if (session.engine.isFinished) {
                    finishLocalSession(forceFinish = false)
                }
            }

            WorkoutCommand.PAUSE -> {
                if (session.engine.state is EngineState.Running) {
                    session.engine.pause(now)
                    exerciseManager.pauseExercise()
                }
            }

            WorkoutCommand.RESUME -> {
                if (session.engine.state is EngineState.Paused) {
                    session.engine.resume(now)
                    exerciseManager.resumeExercise()
                }
            }

            WorkoutCommand.END -> finishLocalSession(forceFinish = true)
        }
    }

    fun startWatchWorkout(template: WorkoutTemplate) {
        val missingPermissions = context.missingWatchWorkoutPermissions(template)
        if (missingPermissions.isNotEmpty()) {
            pendingStartTemplate = template
            pendingPermissionRequest = missingPermissions
            return
        }
        stopRelayMonitor()
        now = Instant.now()
        val engine = WorkoutEngine(template)
        engine.start(now)
        localSession = WearSession(
            template = template,
            engine = engine,
            origin = WorkoutOrigin.WATCH,
        )
        destination = WearDestination.ACTIVE_LOCAL
        syncCoordinator.sendWorkoutStarted(template, WorkoutOrigin.WATCH)
        exerciseManager.startExercise(useGps = template.totalRunDistanceMeters > 0.0)
    }

    DisposableEffect(Unit) {
        refreshTemplates()
        refreshHistory()

        exerciseManager.exerciseListener = ExerciseSessionListener { snapshot ->
            val session = localSession
            if (session != null && session.origin == WorkoutOrigin.WATCH) {
                snapshot.latestLocation?.let(session.engine::ingest)
                snapshot.latestHeartRateBpm?.let { bpm ->
                    session.engine.ingest(
                        HeartRateSample(
                            timestamp = Instant.now(),
                            bpm = bpm,
                        ),
                    )
                }
            }
        }
        heartRateMonitor.heartRateListener = HeartRateListener { bpm, measuredAt ->
            syncCoordinator.sendHeartRateRelay(
                relay = com.bbdyno.hyroxsim.android.core.sync.HeartRateRelay(
                    bpm = bpm,
                    timestamp = measuredAt,
                ),
            )
        }

        syncCoordinator.onReachabilityChanged = { reachable ->
            isReachable = reachable
        }
        syncCoordinator.onReceiveTemplate = { template ->
            if (!template.isBuiltIn) {
                library.saveTemplate(template)
                refreshTemplates()
            }
        }
        syncCoordinator.onReceiveTemplateDeleted = { id ->
            if (library.deleteTemplate(id)) {
                refreshTemplates()
            }
        }
        syncCoordinator.onReceiveCompletedWorkout = { workout ->
            library.saveCompletedWorkout(workout)
            refreshHistory()
            selectedSummary = workout
            destination = WearDestination.SUMMARY
        }
        syncCoordinator.onWorkoutStarted = { _, origin ->
            if (origin == WorkoutOrigin.PHONE && localSession == null) {
                destination = WearDestination.ACTIVE_MIRROR
                val missingPermissions = context.missingWatchHeartRateRelayPermissions()
                if (missingPermissions.isNotEmpty()) {
                    pendingRelayPermissionRequest = true
                    pendingPermissionRequest = missingPermissions
                } else if (!heartRateRelayActive) {
                    heartRateMonitor.start()
                    heartRateRelayActive = true
                }
            }
        }
        syncCoordinator.onLiveStateReceived = { state ->
            if (state.origin == WorkoutOrigin.PHONE && localSession == null) {
                mirrorState = state
                destination = WearDestination.ACTIVE_MIRROR
            }
        }
        syncCoordinator.onWorkoutFinished = { origin ->
            if (origin == WorkoutOrigin.PHONE) {
                stopRelayMonitor()
                mirrorState = null
                if (destination == WearDestination.ACTIVE_MIRROR) {
                    destination = WearDestination.HISTORY
                }
            }
        }
        syncCoordinator.onReceiveCommand = { command ->
            applyLocalCommand(command)
        }

        onDispose {
            exerciseManager.exerciseListener = null
            heartRateMonitor.heartRateListener = null
            syncCoordinator.onReceiveTemplate = null
            syncCoordinator.onReceiveCompletedWorkout = null
            syncCoordinator.onReceiveTemplateDeleted = null
            syncCoordinator.onWorkoutStarted = null
            syncCoordinator.onLiveStateReceived = null
            syncCoordinator.onWorkoutFinished = null
            syncCoordinator.onReceiveCommand = null
            syncCoordinator.onReachabilityChanged = null
            stopRelayMonitor()
        }
    }

    LaunchedEffect(localSession) {
        while (localSession != null) {
            now = Instant.now()
            currentLocalState()?.let(syncCoordinator::sendLiveState)
            delay(1_000)
        }
    }

    LaunchedEffect(pendingPermissionRequest) {
        pendingPermissionRequest?.let { permissions ->
            permissionLauncher.launch(permissions.toTypedArray())
            pendingPermissionRequest = null
        }
    }

    LaunchedEffect(startApproved) {
        if (startApproved) {
            startApproved = false
            val template = pendingStartTemplate
            pendingStartTemplate = null
            pendingRelayPermissionRequest = false
            template?.let(::startWatchWorkout)
        }
    }

    LaunchedEffect(relayApproved) {
        if (relayApproved) {
            relayApproved = false
            pendingRelayPermissionRequest = false
            if (destination == WearDestination.ACTIVE_MIRROR && !heartRateRelayActive) {
                heartRateMonitor.start()
                heartRateRelayActive = true
            }
        }
    }

    LaunchedEffect(permissionNotice) {
        permissionNotice?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            permissionNotice = null
        }
    }

    BackHandler(enabled = destination != WearDestination.HOME) {
        destination = WearDestination.HOME
    }

    val pairedLabel = if (isReachable) "Phone linked" else "Phone offline"

    when (destination) {
        WearDestination.HOME -> HomeWearFeatureScreen(
            templates = templates,
            pairedLabel = pairedLabel,
            onOpenHistory = { destination = WearDestination.HISTORY },
            onSelectTemplate = { template ->
                selectedTemplate = template
                destination = WearDestination.CONFIRM_START
            },
        )

        WearDestination.CONFIRM_START -> selectedTemplate?.let { template ->
            ConfirmStartWearScreen(
                template = template,
                onStartWorkout = { startWatchWorkout(template) },
            )
        }

        WearDestination.HISTORY -> HistoryWearScreen(
            workouts = history,
            onSelectWorkout = {
                selectedSummary = it
                destination = WearDestination.SUMMARY
            },
        )

        WearDestination.SUMMARY -> selectedSummary?.let {
            SummaryWearScreen(workout = it)
        }

        WearDestination.ACTIVE_LOCAL -> currentLocalState()?.let { state ->
            ActiveWearScreen(
                state = state,
                modeLabel = "Watch authoritative",
                onAdvance = {
                    val session = localSession
                    if (session != null) {
                        now = Instant.now()
                        session.engine.advance(now)
                        if (session.engine.isFinished) {
                            finishLocalSession(forceFinish = false)
                        }
                    }
                },
                onPauseResume = {
                    val session = localSession
                    if (session != null) {
                        now = Instant.now()
                        when (session.engine.state) {
                            is EngineState.Running -> {
                                session.engine.pause(now)
                                exerciseManager.pauseExercise()
                            }

                            is EngineState.Paused -> {
                                session.engine.resume(now)
                                exerciseManager.resumeExercise()
                            }

                            else -> Unit
                        }
                    }
                },
                onEnd = {
                    now = Instant.now()
                    finishLocalSession(forceFinish = true)
                },
            )
        }

        WearDestination.ACTIVE_MIRROR -> mirrorState?.let { state ->
            ActiveWearScreen(
                state = state,
                modeLabel = "Phone mirror",
                onAdvance = { syncCoordinator.sendCommand(WorkoutCommand.ADVANCE) },
                onPauseResume = {
                    syncCoordinator.sendCommand(
                        if (state.isPaused) WorkoutCommand.RESUME else WorkoutCommand.PAUSE,
                    )
                },
                onEnd = { syncCoordinator.sendCommand(WorkoutCommand.END) },
            )
        }
    }
}

private fun buildWearLiveState(
    template: WorkoutTemplate,
    engine: WorkoutEngine,
    now: Instant,
    origin: WorkoutOrigin,
): LiveWorkoutState {
    val segmentIndex = engine.currentSegmentIndex ?: template.segments.lastIndex
    val segment = engine.currentSegment ?: template.segments.last()
    val measurements = engine.liveMeasurementsSnapshot
    val latestHeartRate = measurements.heartRateSamples.lastOrNull()?.bpm
    val segmentElapsed = engine.segmentElapsed(now)
    val pace = measurements.averagePaceSecondsPerKm(segmentElapsed)

    return LiveWorkoutState(
        segmentLabel = wearSegmentLabel(template, segment, segmentIndex),
        segmentSubLabel = wearSegmentSubLabel(segment),
        segmentElapsedText = DurationFormatter.ms(segmentElapsed),
        totalElapsedText = DurationFormatter.hms(engine.totalElapsed(now)),
        paceText = DurationFormatter.pace(pace),
        distanceText = DistanceFormatter.short(measurements.distanceMeters),
        heartRateText = latestHeartRate?.let { "$it bpm" } ?: "—",
        heartRateZoneRaw = latestHeartRate,
        stationNameText = segment.stationKind?.displayName,
        stationTargetText = segment.stationTarget?.formatted,
        accentKindRaw = segment.type.name,
        isPaused = engine.state is EngineState.Paused,
        isFinished = engine.isFinished,
        isLastSegment = engine.isLastSegment,
        gpsStrong = segment.type.tracksLocation && measurements.locationSamples.isNotEmpty(),
        gpsActive = segment.type.tracksLocation,
        templateName = template.name,
        totalSegmentCount = template.segments.size,
        currentSegmentIndex = segmentIndex,
        origin = origin,
    )
}

private fun wearSegmentLabel(
    template: WorkoutTemplate,
    segment: WorkoutSegment,
    index: Int,
): String =
    when (segment.type) {
        SegmentType.RUN -> {
            val runNumber = template.segments
                .take(index + 1)
                .count { it.type == SegmentType.RUN }
            "Run $runNumber"
        }

        SegmentType.ROX_ZONE -> "Roxzone"
        SegmentType.STATION -> segment.stationKind?.displayName ?: "Station"
    }

private fun wearSegmentSubLabel(segment: WorkoutSegment): String? =
    when (segment.type) {
        SegmentType.RUN -> segment.distanceMeters?.let(DistanceFormatter::short)
        SegmentType.ROX_ZONE -> "Transition"
        SegmentType.STATION -> segment.stationTarget?.formatted
    }
