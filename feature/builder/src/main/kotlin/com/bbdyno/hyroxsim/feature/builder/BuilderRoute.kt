package com.bbdyno.hyroxsim.feature.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import com.bbdyno.hyroxsim.core.domain.SegmentType
import com.bbdyno.hyroxsim.core.domain.StationKind
import com.bbdyno.hyroxsim.core.domain.WorkoutSegment
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate

/**
 * Custom workout builder matching iOS `WorkoutBuilderViewController`:
 * - BUILDER stats card
 * - Name + division chips
 * - ROX Zone toggle
 * - Editable segment list (reorder via up/down, per-row delete, Add Run/Station)
 * - Save + Garmin sync
 * - Saved templates list
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuilderRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    vm: BuilderViewModel = hiltViewModel(),
) {
    val ui by vm.ui.collectAsState()
    val saved by vm.savedTemplates.collectAsState(initial = emptyList())

    var addStationDialog by remember { mutableStateOf(false) }

    // Preview template reflecting current logical segments + rox zone flag.
    val previewSegments = WorkoutTemplate.materialize(
        logicalSegments = ui.logicalSegments,
        usesRoxZone = ui.usesRoxZone,
    )
    val previewTemplate = WorkoutTemplate(
        name = ui.name,
        division = ui.division,
        segments = previewSegments,
        usesRoxZone = ui.usesRoxZone,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.builder_back), tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.builder_title), color = Color(0xFFFFD700), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = vm::onResetToPreset) {
                Text(stringResource(R.string.builder_reset), color = Color(0xFFAAAAAA), fontSize = 12.sp)
            }
        }

        StatsCard(template = previewTemplate)

        OutlinedTextField(
            value = ui.name,
            onValueChange = vm::onNameChanged,
            label = { Text(stringResource(R.string.builder_name_label)) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF0C0C0C),
                unfocusedContainerColor = Color(0xFF0C0C0C),
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFFAAAAAA),
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Text(stringResource(R.string.builder_section_division), color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HyroxDivision.entries.forEach { d ->
                FilterChip(
                    selected = ui.division == d,
                    onClick = { vm.onDivisionChanged(d) },
                    label = { Text(d.shortName) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF0C0C0C),
                        selectedContainerColor = Color(0xFFFFD700),
                        labelColor = Color.White,
                        selectedLabelColor = Color.Black,
                    ),
                )
            }
        }

        RoxZoneCard(enabled = ui.usesRoxZone, onToggle = vm::onRoxZoneToggled)

        // Editable logical segments (no ROX zones — materialised at save time)
        Text(stringResource(R.string.builder_section_segments), color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(
            "${ui.logicalSegments.size} · ${previewSegments.size}",
            color = Color(0xFF666666),
            fontSize = 11.sp,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ui.logicalSegments.forEachIndexed { index, seg ->
                EditableSegmentRow(
                    index = index,
                    segment = seg,
                    canMoveUp = index > 0,
                    canMoveDown = index < ui.logicalSegments.lastIndex,
                    onUp = { vm.onMoveUp(index) },
                    onDown = { vm.onMoveDown(index) },
                    onDelete = { vm.onDeleteSegment(index) },
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(
                onClick = vm::onAddRun,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.builder_add_run))
            }
            OutlinedButton(
                onClick = { addStationDialog = true },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.builder_add_station))
            }
        }

        if (addStationDialog) {
            StationPickerDialog(
                onDismiss = { addStationDialog = false },
                onSelect = { kind ->
                    vm.onAddStation(kind)
                    addStationDialog = false
                },
            )
        }

        Button(
            onClick = vm::onSave,
            enabled = !ui.saving && ui.logicalSegments.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (ui.saving) stringResource(R.string.builder_saving) else stringResource(R.string.builder_save),
                fontWeight = FontWeight.Bold,
            )
        }

        ui.savedMessage?.let { msg ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(msg, color = Color(0xFFAAAAAA), fontSize = 12.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = vm::clearMessage) { Text(stringResource(R.string.builder_close), color = Color(0xFFFFD700)) }
            }
        }

        if (saved.isNotEmpty()) {
            HorizontalDivider(color = Color(0xFF222222), modifier = Modifier.padding(top = 12.dp))
            Text(stringResource(R.string.builder_section_saved), color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            saved.forEach { t ->
                Surface(
                    color = Color(0xFF0C0C0C),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(t.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (t.usesRoxZone)
                                    stringResource(R.string.builder_template_segments_on, t.segments.size)
                                else
                                    stringResource(R.string.builder_template_segments_off, t.segments.size),
                                color = Color(0xFF888888),
                                fontSize = 12.sp,
                            )
                        }
                        TextButton(onClick = { vm.delete(t) }) {
                            Text(stringResource(R.string.builder_delete), color = Color(0xFFFF3B30))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun EditableSegmentRow(
    index: Int,
    segment: WorkoutSegment,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onDelete: () -> Unit,
) {
    val color = when (segment.type) {
        SegmentType.Run -> Color(0xFF007AFF)
        SegmentType.Station -> Color(0xFFFFD700)
        SegmentType.RoxZone -> Color(0xFFFF9500)
    }
    val label = when (segment.type) {
        SegmentType.Run -> stringResource(R.string.builder_segment_run, segment.distanceMeters?.toInt() ?: 1000)
        SegmentType.Station -> segment.stationKind?.displayName ?: stringResource(R.string.builder_segment_station_fallback)
        SegmentType.RoxZone -> stringResource(R.string.builder_segment_rox)
    }
    Surface(
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "%02d".format(index + 1),
                color = Color(0xFF555555),
                fontSize = 10.sp,
                modifier = Modifier.width(22.dp),
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(22.dp)
                    .background(color, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                segment.weightKg?.let {
                    Text(
                        "${it.toInt()} kg${segment.weightNote?.let { n -> " · $n" } ?: ""}",
                        color = Color(0xFF888888),
                        fontSize = 10.sp,
                    )
                }
            }
            IconButton(onClick = onUp, enabled = canMoveUp) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.builder_row_move_up),
                    tint = if (canMoveUp) Color(0xFFAAAAAA) else Color(0xFF333333),
                )
            }
            IconButton(onClick = onDown, enabled = canMoveDown) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.builder_row_move_down),
                    tint = if (canMoveDown) Color(0xFFAAAAAA) else Color(0xFF333333),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.builder_row_delete),
                    tint = Color(0xFFFF3B30),
                )
            }
        }
    }
}

@Composable
private fun StationPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (StationKind) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.builder_dialog_cancel), color = Color(0xFFFFD700)) }
        },
        containerColor = Color(0xFF0C0C0C),
        title = {
            Text(stringResource(R.string.builder_dialog_choose_station), color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StationKind.standardOrder.forEach { kind ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(kind) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFFFD700), RoundedCornerShape(2.dp)),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(kind.displayName, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        },
    )
}

@Composable
private fun StatsCard(template: WorkoutTemplate) {
    val runDistanceKm = template.totalRunDistanceMeters / 1000
    val stationCount = template.stationCount
    val estSec = template.estimatedDurationSeconds.toInt()
    Surface(
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            StatColumn(label = stringResource(R.string.builder_stat_stations), value = stationCount.toString())
            StatColumn(label = stringResource(R.string.builder_stat_run), value = stringResource(R.string.builder_stat_run_value, runDistanceKm))
            StatColumn(label = stringResource(R.string.builder_stat_est_time), value = formatSec(estSec))
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column {
        Text(label, color = Color(0xFF888888), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(
            value,
            color = Color(0xFFFFD700),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun RoxZoneCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.builder_rox_label), fontWeight = FontWeight.SemiBold)
                Text(
                    if (enabled) stringResource(R.string.builder_rox_on)
                    else stringResource(R.string.builder_rox_off),
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = Color(0xFFFFD700),
                ),
            )
        }
    }
}

private fun formatSec(total: Int): String {
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}
