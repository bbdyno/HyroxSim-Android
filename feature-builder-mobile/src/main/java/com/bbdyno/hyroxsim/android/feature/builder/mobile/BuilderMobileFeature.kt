//
//  BuilderMobileFeature.kt
//  feature-builder-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.builder.mobile

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bbdyno.hyroxsim.android.core.model.HyroxDivision
import com.bbdyno.hyroxsim.android.core.model.SegmentType
import com.bbdyno.hyroxsim.android.core.model.StationKind
import com.bbdyno.hyroxsim.android.core.model.StationTarget
import com.bbdyno.hyroxsim.android.core.model.WorkoutSegment
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import java.util.UUID
import kotlin.math.roundToInt

object BuilderMobileFeatureInfo {
    const val name: String = "feature-builder-mobile"
}

private enum class TargetKind {
    DISTANCE,
    REPS,
    DURATION,
    NONE,
}

private val supportedStationKinds: List<StationKind> = listOf(
    StationKind.SkiErg,
    StationKind.SledPush,
    StationKind.SledPull,
    StationKind.BurpeeBroadJumps,
    StationKind.Rowing,
    StationKind.FarmersCarry,
    StationKind.SandbagLunges,
    StationKind.WallBalls,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuilderMobileScreen(
    startingTemplate: WorkoutTemplate,
    onSaveTemplate: (WorkoutTemplate) -> Unit,
    onStartWorkout: (WorkoutTemplate) -> Unit,
) {
    var draftName by remember(startingTemplate.id) {
        mutableStateOf(builderSeedName(startingTemplate))
    }
    var selectedDivision by remember(startingTemplate.id) {
        mutableStateOf(startingTemplate.division)
    }
    val segments = remember(startingTemplate.id) {
        mutableStateListOf<WorkoutSegment>().apply {
            addAll(startingTemplate.segments.map(WorkoutSegment::cloneForBuilder))
        }
    }
    var validationMessage by remember(startingTemplate.id) { mutableStateOf<String?>(null) }

    fun replaceWithTemplate(template: WorkoutTemplate) {
        draftName = builderSeedName(template)
        selectedDivision = template.division
        segments.clear()
        segments.addAll(template.segments.map(WorkoutSegment::cloneForBuilder))
        validationMessage = null
    }

    fun draftTemplate(): WorkoutTemplate =
        WorkoutTemplate(
            name = draftName.trim().ifBlank { "My Workout" },
            division = selectedDivision,
            segments = segments.toList(),
            isBuiltIn = false,
        )

    fun performIfValid(block: (WorkoutTemplate) -> Unit) {
        val template = draftTemplate()
        validationMessage = runCatching {
            template.validate()
            block(template)
            null
        }.getOrElse { it.message ?: "Template validation failed." }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Workout Builder", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = {
                            draftName = it
                            validationMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Template Name") },
                        supportingText = {
                            Text("Edit segments, save as a template, or start immediately.")
                        },
                    )
                    BuilderMetaCard(segments = segments)
                    validationMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        item {
            Text("Preset Base", style = MaterialTheme.typography.titleMedium)
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HyroxDivision.entries.forEach { division ->
                    FilterChip(
                        selected = division == selectedDivision,
                        onClick = {
                            selectedDivision = division
                            replaceWithTemplate(
                                com.bbdyno.hyroxsim.android.core.model.HyroxPresets.template(division),
                            )
                        },
                        label = { Text(division.shortName) },
                    )
                }
            }
        }

        item {
            Text("Segments", style = MaterialTheme.typography.titleMedium)
        }

        if (segments.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("No segments yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Add a run, Rox Zone, or station below to build a custom workout.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        itemsIndexed(segments, key = { _, segment -> segment.id }) { index, segment ->
            SegmentEditorCard(
                segment = segment,
                index = index,
                stationOrdinal = segments
                    .take(index + 1)
                    .count { it.type == SegmentType.STATION },
                canMoveUp = index > 0,
                canMoveDown = index < segments.lastIndex,
                onMoveUp = {
                    val current = segments[index]
                    segments[index] = segments[index - 1]
                    segments[index - 1] = current
                },
                onMoveDown = {
                    val current = segments[index]
                    segments[index] = segments[index + 1]
                    segments[index + 1] = current
                },
                onDelete = {
                    segments.removeAt(index)
                },
                onSegmentChanged = { updated ->
                    segments[index] = updated
                },
            )
        }

        item {
            Text("Add Segment", style = MaterialTheme.typography.titleMedium)
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(onClick = { segments.add(WorkoutSegment.run()) }) {
                    Text("Add Run")
                }
                Button(onClick = { segments.add(WorkoutSegment.roxZone()) }) {
                    Text("Add Roxzone")
                }
                Button(
                    onClick = {
                        val defaultKind = supportedStationKinds.first()
                        segments.add(
                            WorkoutSegment.station(
                                kind = defaultKind,
                                target = defaultKind.defaultTarget,
                            ),
                        )
                    },
                ) {
                    Text("Add Station")
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { performIfValid(onSaveTemplate) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save Template")
                }
                OutlinedButton(
                    onClick = { performIfValid(onStartWorkout) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Start Now")
                }
            }
        }
    }
}

@Composable
private fun BuilderMetaCard(
    segments: List<WorkoutSegment>,
) {
    val template = WorkoutTemplate(
        name = "Draft",
        segments = segments,
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("${template.stationCount} stations", style = MaterialTheme.typography.bodyMedium)
            Text(formatRunDistance(template.totalRunDistanceMeters), style = MaterialTheme.typography.bodyMedium)
            Text("~${(template.estimatedDurationSeconds / 60.0).roundToInt()} min", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SegmentEditorCard(
    segment: WorkoutSegment,
    index: Int,
    stationOrdinal: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onSegmentChanged: (WorkoutSegment) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        segmentHeader(segment = segment, index = index, stationOrdinal = stationOrdinal),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    segmentSummary(segment)?.let { summary ->
                        Text(summary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(onClick = onMoveUp, enabled = canMoveUp) {
                        Text("Up")
                    }
                    OutlinedButton(onClick = onMoveDown, enabled = canMoveDown) {
                        Text("Down")
                    }
                    OutlinedButton(onClick = onDelete) {
                        Text("Delete")
                    }
                }
            }

            when (segment.type) {
                SegmentType.RUN -> RunSegmentEditor(segment = segment, onSegmentChanged = onSegmentChanged)
                SegmentType.ROX_ZONE -> Text(
                    "Rox Zone transitions stay as timed transition segments.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                SegmentType.STATION -> StationSegmentEditor(segment = segment, onSegmentChanged = onSegmentChanged)
            }
        }
    }
}

@Composable
private fun RunSegmentEditor(
    segment: WorkoutSegment,
    onSegmentChanged: (WorkoutSegment) -> Unit,
) {
    var distanceText by remember(segment.id) {
        mutableStateOf(((segment.distanceMeters ?: 1000.0).roundToInt()).toString())
    }

    OutlinedTextField(
        value = distanceText,
        onValueChange = { value ->
            distanceText = value
            value.toDoubleOrNull()?.let { meters ->
                onSegmentChanged(segment.copy(distanceMeters = meters.coerceAtLeast(1.0)))
            }
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Run Distance (meters)") },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StationSegmentEditor(
    segment: WorkoutSegment,
    onSegmentChanged: (WorkoutSegment) -> Unit,
) {
    val selectedKind = segment.stationKind ?: StationKind.SkiErg
    var targetValueInput by remember(segment.id) { mutableStateOf(formatTargetValue(segment.stationTarget)) }
    var weightText by remember(segment.id) { mutableStateOf(segment.weightKg?.roundToInt()?.toString().orEmpty()) }
    var noteText by remember(segment.id) { mutableStateOf(segment.weightNote.orEmpty()) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            supportedStationKinds.forEach { kind ->
                FilterChip(
                    selected = selectedKind == kind,
                    onClick = {
                        targetValueInput = formatTargetValue(kind.defaultTarget)
                        onSegmentChanged(
                            segment.copy(
                                stationKind = kind,
                                stationTarget = kind.defaultTarget,
                            ),
                        )
                    },
                    label = { Text(kind.displayName) },
                )
            }
        }

        val targetKind = targetKind(segment.stationTarget)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TargetKind.entries.forEach { candidate ->
                FilterChip(
                    selected = candidate == targetKind,
                    onClick = {
                        val nextTarget = defaultTargetFor(selectedKind, candidate)
                        targetValueInput = formatTargetValue(nextTarget)
                        onSegmentChanged(segment.copy(stationTarget = nextTarget))
                    },
                    label = { Text(candidate.label) },
                )
            }
        }

        OutlinedTextField(
            value = targetValueInput,
            onValueChange = { value ->
                targetValueInput = value
                parseTargetInput(value, targetKind)?.let { parsed ->
                    onSegmentChanged(segment.copy(stationTarget = parsed))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(targetFieldLabel(targetKind)) },
            enabled = targetKind != TargetKind.NONE,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { value ->
                    weightText = value
                    onSegmentChanged(
                        segment.copy(
                            weightKg = value.toDoubleOrNull(),
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
                label = { Text("Weight (kg)") },
            )
            OutlinedTextField(
                value = noteText,
                onValueChange = { value ->
                    noteText = value
                    onSegmentChanged(
                        segment.copy(
                            weightNote = value.ifBlank { null },
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
                label = { Text("Weight Note") },
            )
        }
    }
}

private fun builderSeedName(template: WorkoutTemplate): String =
    if (template.isBuiltIn) {
        "Custom from ${template.name}"
    } else {
        template.name
    }

private fun WorkoutSegment.cloneForBuilder(): WorkoutSegment =
    copy(id = UUID.randomUUID())

private fun segmentHeader(
    segment: WorkoutSegment,
    index: Int,
    stationOrdinal: Int,
): String =
    when (segment.type) {
        SegmentType.RUN -> "Segment ${index + 1} • Run"
        SegmentType.ROX_ZONE -> "Segment ${index + 1} • Rox Zone"
        SegmentType.STATION -> "Station ${stationOrdinal.toString().padStart(2, '0')}"
    }

private fun segmentSummary(segment: WorkoutSegment): String? =
    when (segment.type) {
        SegmentType.RUN -> segment.distanceMeters?.let { "${it.roundToInt()} m" }
        SegmentType.ROX_ZONE -> "Transition segment"
        SegmentType.STATION -> buildString {
            segment.stationTarget?.formatted?.let(::append)
            segment.weightKg?.let { weight ->
                if (isNotEmpty()) append(" • ")
                append(weight.roundToInt())
                append(" kg")
            }
            segment.weightNote?.takeIf { it.isNotBlank() }?.let {
                if (isNotEmpty()) append(" • ")
                append(it)
            }
        }.ifBlank { null }
    }

private val TargetKind.label: String
    get() = when (this) {
        TargetKind.DISTANCE -> "Distance"
        TargetKind.REPS -> "Reps"
        TargetKind.DURATION -> "Duration"
        TargetKind.NONE -> "None"
    }

private fun targetKind(target: StationTarget?): TargetKind =
    when (target) {
        is StationTarget.Distance -> TargetKind.DISTANCE
        is StationTarget.Reps -> TargetKind.REPS
        is StationTarget.Duration -> TargetKind.DURATION
        else -> TargetKind.NONE
    }

private fun formatTargetValue(target: StationTarget?): String =
    when (target) {
        is StationTarget.Distance -> target.meters.roundToInt().toString()
        is StationTarget.Reps -> target.count.toString()
        is StationTarget.Duration -> target.seconds.roundToInt().toString()
        else -> ""
    }

private fun defaultTargetFor(
    stationKind: StationKind,
    targetKind: TargetKind,
): StationTarget =
    when (targetKind) {
        TargetKind.DISTANCE -> stationKind.defaultTarget.takeIf { it is StationTarget.Distance }
            ?: StationTarget.distance(100.0)

        TargetKind.REPS -> stationKind.defaultTarget.takeIf { it is StationTarget.Reps }
            ?: StationTarget.reps(10)

        TargetKind.DURATION -> StationTarget.duration(60.0)
        TargetKind.NONE -> StationTarget.none()
    }

private fun parseTargetInput(
    rawValue: String,
    targetKind: TargetKind,
): StationTarget? =
    when (targetKind) {
        TargetKind.DISTANCE -> rawValue.toDoubleOrNull()?.let(StationTarget::distance)
        TargetKind.REPS -> rawValue.toIntOrNull()?.let(StationTarget::reps)
        TargetKind.DURATION -> rawValue.toDoubleOrNull()?.let(StationTarget::duration)
        TargetKind.NONE -> StationTarget.none()
    }

private fun targetFieldLabel(targetKind: TargetKind): String =
    when (targetKind) {
        TargetKind.DISTANCE -> "Distance (meters)"
        TargetKind.REPS -> "Reps"
        TargetKind.DURATION -> "Duration (seconds)"
        TargetKind.NONE -> "No target"
    }

private fun formatRunDistance(meters: Double): String =
    when {
        meters >= 1000.0 -> String.format("%.1f km", meters / 1000.0)
        else -> "${meters.roundToInt()} m"
    }
