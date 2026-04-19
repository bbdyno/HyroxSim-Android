package com.bbdyno.hyroxsim.feature.builder

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import com.bbdyno.hyroxsim.core.domain.SegmentType
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate

/**
 * Custom workout builder mirroring iOS `WorkoutBuilderViewController`:
 * - BUILDER stats card (station count, run distance, estimated time)
 * - Name + division chips
 * - ROX Zone toggle card
 * - Live-materialised segment preview (read-only in this pass;
 *   segment-level edit/reorder is a follow-up)
 * - Save + Garmin sync button
 * - Saved templates list with delete
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

    // Compute live preview template from current builder state
    val previewTemplate = WorkoutTemplate.hyroxPreset(ui.division).let { preset ->
        preset.copy(
            segments = WorkoutTemplate.materialize(
                logicalSegments = preset.logicalSegments,
                usesRoxZone = ui.usesRoxZone,
            ),
            usesRoxZone = ui.usesRoxZone,
        )
    }

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
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text("Builder", color = Color(0xFFFFD700), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        StatsCard(template = previewTemplate)

        OutlinedTextField(
            value = ui.name,
            onValueChange = vm::onNameChanged,
            label = { Text("Name") },
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

        Text("DIVISION", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

        Text("SEGMENTS", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        SegmentPreview(template = previewTemplate)

        Button(
            onClick = vm::onSave,
            enabled = !ui.saving,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (ui.saving) "Saving…" else "Save + Sync to Garmin",
                fontWeight = FontWeight.Bold,
            )
        }

        ui.savedMessage?.let { msg ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(msg, color = Color(0xFFAAAAAA), fontSize = 12.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = vm::clearMessage) { Text("Close", color = Color(0xFFFFD700)) }
            }
        }

        if (saved.isNotEmpty()) {
            HorizontalDivider(color = Color(0xFF222222), modifier = Modifier.padding(top = 12.dp))
            Text("SAVED", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                "${t.segments.size} segments · ROX ${if (t.usesRoxZone) "on" else "off"}",
                                color = Color(0xFF888888),
                                fontSize = 12.sp,
                            )
                        }
                        TextButton(onClick = { vm.delete(t) }) {
                            Text("Delete", color = Color(0xFFFF3B30))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
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
            StatColumn(label = "STATIONS", value = stationCount.toString())
            StatColumn(label = "RUN", value = "%.1f km".format(runDistanceKm))
            StatColumn(label = "EST. TIME", value = formatSec(estSec))
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
                Text("ROX Zone", fontWeight = FontWeight.SemiBold)
                Text(
                    if (enabled) "Insert transition between run ↔ station"
                    else "Direct run → station, no transition",
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

@Composable
private fun SegmentPreview(template: WorkoutTemplate) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        template.segments.forEachIndexed { index, seg ->
            val color = when (seg.type) {
                SegmentType.Run -> Color(0xFF007AFF)
                SegmentType.RoxZone -> Color(0xFFFF9500)
                SegmentType.Station -> Color(0xFFFFD700)
            }
            val label = when (seg.type) {
                SegmentType.Run -> "Run ${seg.distanceMeters?.toInt() ?: 1000}m"
                SegmentType.RoxZone -> "ROX Zone"
                SegmentType.Station -> seg.stationKind?.displayName ?: "Station"
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
            ) {
                Text(
                    text = "%02d".format(index + 1),
                    color = Color(0xFF555555),
                    fontSize = 10.sp,
                    modifier = Modifier.width(24.dp),
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .background(color, RoundedCornerShape(2.dp)),
                )
                Spacer(Modifier.width(8.dp))
                Text(label, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                val goalSec = seg.goalDurationSeconds?.toInt() ?: 0
                Text(
                    formatSec(goalSec),
                    color = Color(0xFF666666),
                    fontSize = 11.sp,
                )
            }
        }
    }
}

private fun formatSec(total: Int): String {
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}
