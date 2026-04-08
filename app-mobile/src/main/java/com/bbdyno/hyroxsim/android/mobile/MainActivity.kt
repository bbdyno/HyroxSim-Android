//
//  MainActivity.kt
//  app-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.mobile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.bbdyno.hyroxsim.android.core.model.HyroxPresets
import com.bbdyno.hyroxsim.android.core.model.SegmentType
import com.bbdyno.hyroxsim.android.core.model.WorkoutSegment
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import com.bbdyno.hyroxsim.android.core.sync.LiveWorkoutState
import com.bbdyno.hyroxsim.android.core.sync.WorkoutCommand
import com.bbdyno.hyroxsim.android.core.sync.WorkoutOrigin
import com.bbdyno.hyroxsim.android.data.datalayer.WearDataLayerSyncCoordinator
import com.bbdyno.hyroxsim.android.data.datalayer.WearOsDataLayerTransport
import com.bbdyno.hyroxsim.android.data.local.LocalWorkoutLibrary
import com.bbdyno.hyroxsim.android.feature.active.mobile.ActiveMobileScreen
import com.bbdyno.hyroxsim.android.feature.builder.mobile.BuilderMobileScreen
import com.bbdyno.hyroxsim.android.feature.history.mobile.HistoryMobileScreen
import com.bbdyno.hyroxsim.android.feature.home.mobile.HomeMobileScreen
import com.bbdyno.hyroxsim.android.feature.home.mobile.TemplateDetailMobileScreen
import com.bbdyno.hyroxsim.android.feature.summary.mobile.SummaryMobileScreen
import java.time.Instant
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val library = LocalWorkoutLibrary(applicationContext).also { it.seedIfEmpty() }
        val syncCoordinator = WearDataLayerSyncCoordinator(
            transport = WearOsDataLayerTransport(applicationContext),
        ).also { it.activate() }
        val phoneLocationTracker = PhoneLocationTracker(applicationContext)

        setContent {
            MaterialTheme {
                MobileApp(
                    library = library,
                    syncCoordinator = syncCoordinator,
                    phoneLocationTracker = phoneLocationTracker,
                )
            }
        }
    }
}

private enum class MobileDestination {
    HOME,
    TEMPLATE_DETAIL,
    BUILDER,
    HISTORY,
    SUMMARY,
    ACTIVE_LOCAL,
    ACTIVE_MIRROR,
}

private data class MobileSession(
    val template: WorkoutTemplate,
    val engine: WorkoutEngine,
    val origin: WorkoutOrigin,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MobileApp(
    library: LocalWorkoutLibrary,
    syncCoordinator: WearDataLayerSyncCoordinator,
    phoneLocationTracker: PhoneLocationTracker,
) {
    val context = LocalContext.current
    val templates = remember { mutableStateListOf<WorkoutTemplate>() }
    val history = remember { mutableStateListOf<CompletedWorkout>() }
    var destination by remember { mutableStateOf(MobileDestination.HOME) }
    var selectedTemplateDetail by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var builderSeedTemplate by remember {
        mutableStateOf(
            library.lastSelectedDivision()
                ?.let(HyroxPresets::template)
                ?: HyroxPresets.menOpenSingle,
        )
    }
    var selectedSummary by remember { mutableStateOf<CompletedWorkout?>(null) }
    var localSession by remember { mutableStateOf<MobileSession?>(null) }
    var mirrorState by remember { mutableStateOf<LiveWorkoutState?>(null) }
    var isReachable by remember { mutableStateOf(syncCoordinator.isReachable) }
    var now by remember { mutableStateOf(Instant.now()) }
    var pendingPhoneStartTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var permissionNotice by remember { mutableStateOf<String?>(null) }
    var pendingPermissionRequest by remember { mutableStateOf<List<String>?>(null) }
    var permissionGrantedForPendingStart by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants.values.all { it }
        if (granted) {
            permissionGrantedForPendingStart = true
        } else if (pendingPhoneStartTemplate != null) {
            pendingPhoneStartTemplate = null
            permissionNotice = "Phone-origin workouts need location permission to track runs."
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
        return buildLiveState(
            template = session.template,
            engine = session.engine,
            now = now,
            origin = session.origin,
        )
    }

    fun finishLocalSession(forceFinish: Boolean) {
        val session = localSession ?: return
        if (forceFinish && !session.engine.isFinished) {
            session.engine.finish(now)
        }
        if (!session.engine.isFinished) {
            return
        }
        val completedWorkout = session.engine.makeCompletedWorkout()
        library.saveCompletedWorkout(completedWorkout)
        refreshHistory()
        selectedSummary = completedWorkout
        localSession = null
        destination = MobileDestination.SUMMARY

        if (session.origin == WorkoutOrigin.PHONE) {
            syncCoordinator.sendCompletedWorkout(completedWorkout)
            syncCoordinator.sendWorkoutFinished(WorkoutOrigin.PHONE)
        }
    }

    fun applyLocalCommand(command: WorkoutCommand) {
        val session = localSession ?: return
        if (session.origin != WorkoutOrigin.PHONE) {
            return
        }
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
                }
            }

            WorkoutCommand.RESUME -> {
                if (session.engine.state is EngineState.Paused) {
                    session.engine.resume(now)
                }
            }

            WorkoutCommand.END -> finishLocalSession(forceFinish = true)
        }
    }

    fun startPhoneWorkout(template: WorkoutTemplate) {
        val engine = WorkoutEngine(template)
        engine.start(now)
        localSession = MobileSession(
            template = template,
            engine = engine,
            origin = WorkoutOrigin.PHONE,
        )
        destination = MobileDestination.ACTIVE_LOCAL
        syncCoordinator.sendWorkoutStarted(template, WorkoutOrigin.PHONE)
        buildLiveState(
            template = template,
            engine = engine,
            now = now,
            origin = WorkoutOrigin.PHONE,
        ).let(syncCoordinator::sendLiveState)
    }

    fun requestPhoneWorkoutStart(template: WorkoutTemplate) {
        val missingPermissions = context.missingPhoneWorkoutPermissions(template)
        if (missingPermissions.isEmpty()) {
            now = Instant.now()
            selectedTemplateDetail = template
            startPhoneWorkout(template)
        } else {
            pendingPhoneStartTemplate = template
            pendingPermissionRequest = missingPermissions
        }
    }

    fun saveCustomTemplate(template: WorkoutTemplate) {
        library.saveTemplate(template)
        refreshTemplates()
        syncCoordinator.sendTemplate(template)
        selectedTemplateDetail = template
        destination = MobileDestination.TEMPLATE_DETAIL
    }

    DisposableEffect(Unit) {
        refreshTemplates()
        refreshHistory()

        syncCoordinator.onReachabilityChanged = { reachable ->
            isReachable = reachable
        }
        syncCoordinator.onReceiveTemplate = { template ->
            if (!template.isBuiltIn) {
                library.saveTemplate(template)
                refreshTemplates()
                if (selectedTemplateDetail?.id == template.id) {
                    selectedTemplateDetail = template
                }
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
            destination = MobileDestination.SUMMARY
        }
        syncCoordinator.onWorkoutStarted = { _, origin ->
            if (origin == WorkoutOrigin.WATCH && localSession == null) {
                destination = MobileDestination.ACTIVE_MIRROR
            }
        }
        syncCoordinator.onLiveStateReceived = { remoteState ->
            if (remoteState.origin == WorkoutOrigin.WATCH && localSession == null) {
                mirrorState = remoteState
                destination = MobileDestination.ACTIVE_MIRROR
            }
        }
        syncCoordinator.onWorkoutFinished = { origin ->
            if (origin == WorkoutOrigin.WATCH) {
                mirrorState = null
                if (destination == MobileDestination.ACTIVE_MIRROR) {
                    destination = MobileDestination.HISTORY
                }
            }
        }
        syncCoordinator.onReceiveCommand = { command ->
            applyLocalCommand(command)
        }
        syncCoordinator.onHeartRateRelayReceived = { relay ->
            val session = localSession
            if (session != null && session.origin == WorkoutOrigin.PHONE) {
                session.engine.ingest(
                    HeartRateSample(
                        timestamp = relay.timestamp,
                        bpm = relay.bpm,
                    ),
                )
            }
        }

        library.customTemplates().forEach(syncCoordinator::sendTemplate)

        onDispose {
            syncCoordinator.onReceiveTemplate = null
            syncCoordinator.onReceiveCompletedWorkout = null
            syncCoordinator.onReceiveTemplateDeleted = null
            syncCoordinator.onWorkoutStarted = null
            syncCoordinator.onLiveStateReceived = null
            syncCoordinator.onWorkoutFinished = null
            syncCoordinator.onReceiveCommand = null
            syncCoordinator.onHeartRateRelayReceived = null
            syncCoordinator.onReachabilityChanged = null
        }
    }

    DisposableEffect(localSession?.template?.id, localSession?.origin) {
        val shouldTrackLocation = localSession?.origin == WorkoutOrigin.PHONE &&
            localSession?.template?.totalRunDistanceMeters?.let { it > 0.0 } == true &&
            context.missingPhoneWorkoutPermissions(localSession?.template ?: HyroxPresets.menOpenSingle).isEmpty()

        if (shouldTrackLocation) {
            phoneLocationTracker.onLocationSample = { sample ->
                val session = localSession
                if (session?.origin == WorkoutOrigin.PHONE) {
                    session.engine.ingest(sample)
                }
            }
            phoneLocationTracker.start()
        }

        onDispose {
            phoneLocationTracker.stop()
            phoneLocationTracker.onLocationSample = null
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

    LaunchedEffect(permissionGrantedForPendingStart) {
        if (permissionGrantedForPendingStart) {
            permissionGrantedForPendingStart = false
            pendingPhoneStartTemplate?.let { template ->
                pendingPhoneStartTemplate = null
                now = Instant.now()
                selectedTemplateDetail = template
                startPhoneWorkout(template)
            }
        }
    }

    LaunchedEffect(permissionNotice) {
        permissionNotice?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            permissionNotice = null
        }
    }

    val canNavigateBack = destination != MobileDestination.HOME
    val pairedLabel = if (isReachable) {
        "Watch linked"
    } else {
        "Watch not reachable"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle(destination)) },
                navigationIcon = {
                    if (canNavigateBack) {
                        TextButton(
                            onClick = {
                                destination = when (destination) {
                                    MobileDestination.TEMPLATE_DETAIL,
                                    MobileDestination.BUILDER,
                                    MobileDestination.HISTORY,
                                    MobileDestination.SUMMARY,
                                    MobileDestination.ACTIVE_LOCAL,
                                    MobileDestination.ACTIVE_MIRROR,
                                    -> MobileDestination.HOME
                                    MobileDestination.HOME -> MobileDestination.HOME
                                }
                            },
                        ) {
                            Text("Back")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when (destination) {
            MobileDestination.HOME -> HomeMobileScreen(
                templates = templates,
                pairedLabel = pairedLabel,
                onOpenBuilder = {
                    builderSeedTemplate = selectedTemplateDetail
                        ?: library.lastSelectedDivision()?.let(HyroxPresets::template)
                        ?: HyroxPresets.menOpenSingle
                    destination = MobileDestination.BUILDER
                },
                onOpenHistory = { destination = MobileDestination.HISTORY },
                onSelectTemplate = { template ->
                    selectedTemplateDetail = template
                    destination = MobileDestination.TEMPLATE_DETAIL
                },
                onStartPhoneWorkout = ::requestPhoneWorkoutStart,
            )

            MobileDestination.TEMPLATE_DETAIL -> selectedTemplateDetail?.let { template ->
                TemplateDetailMobileScreen(
                    template = template,
                    onStartPhoneWorkout = { requestPhoneWorkoutStart(template) },
                    onCustomizeTemplate = {
                        builderSeedTemplate = template
                        destination = MobileDestination.BUILDER
                    },
                )
            }

            MobileDestination.BUILDER -> BuilderMobileScreen(
                startingTemplate = builderSeedTemplate,
                onDivisionSelected = { division ->
                    library.setLastSelectedDivision(division)
                },
                onSaveTemplate = ::saveCustomTemplate,
                onStartWorkout = { template ->
                    template.division?.let(library::setLastSelectedDivision)
                    requestPhoneWorkoutStart(template)
                },
            )

            MobileDestination.HISTORY -> HistoryMobileScreen(
                workouts = history,
                onSelectWorkout = {
                    selectedSummary = it
                    destination = MobileDestination.SUMMARY
                },
            )

            MobileDestination.SUMMARY -> selectedSummary?.let {
                SummaryMobileScreen(workout = it)
            }

            MobileDestination.ACTIVE_LOCAL -> currentLocalState()?.let { state ->
                ActiveMobileScreen(
                    state = state,
                    modeLabel = if (state.origin == WorkoutOrigin.PHONE) {
                        "Phone is authoritative"
                    } else {
                        "Watch is authoritative"
                    },
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
                                is EngineState.Running -> session.engine.pause(now)
                                is EngineState.Paused -> session.engine.resume(now)
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

            MobileDestination.ACTIVE_MIRROR -> mirrorState?.let { state ->
                ActiveMobileScreen(
                    state = state,
                    modeLabel = "Watch mirrored to phone",
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
}

private fun screenTitle(destination: MobileDestination): String =
    when (destination) {
        MobileDestination.HOME -> "HYROX"
        MobileDestination.TEMPLATE_DETAIL -> "Template"
        MobileDestination.BUILDER -> "Builder"
        MobileDestination.HISTORY -> "History"
        MobileDestination.SUMMARY -> "Summary"
        MobileDestination.ACTIVE_LOCAL -> "Active Workout"
        MobileDestination.ACTIVE_MIRROR -> "Watch Mirror"
    }

private fun buildLiveState(
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
        segmentLabel = segmentLabel(template, segment, segmentIndex),
        segmentSubLabel = segmentSubLabel(segment),
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

private fun segmentLabel(
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

private fun segmentSubLabel(segment: WorkoutSegment): String? =
    when (segment.type) {
        SegmentType.RUN -> segment.distanceMeters?.let(DistanceFormatter::short)
        SegmentType.ROX_ZONE -> "Transition"
        SegmentType.STATION -> segment.stationTarget?.formatted
    }
