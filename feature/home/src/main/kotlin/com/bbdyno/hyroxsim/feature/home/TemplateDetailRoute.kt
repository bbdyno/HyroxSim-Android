package com.bbdyno.hyroxsim.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bbdyno.hyroxsim.core.domain.SegmentType
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate

/**
 * Detail view for a built-in preset or saved template. Mirrors iOS
 * `TemplateDetailViewController`: title + meta, GOALS card (tap to open
 * goal setup), course row list, footer Start button.
 */
@Composable
fun TemplateDetailRoute(
    routeKey: String,
    onBack: () -> Unit,
    onStart: (WorkoutTemplate) -> Unit,
    onOpenGoal: (routeKey: String) -> Unit,
    vm: TemplateDetailViewModel = hiltViewModel(),
) {
    val ui by vm.ui.collectAsState()
    LaunchedEffect(routeKey) { vm.load(routeKey) }

    val template = ui.template
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            if (template == null) {
                Text("Loading…", color = Color(0xFF888888))
                return@Column
            }

            TitleBlock(template)
            GoalCard(
                totalSeconds = ui.goalTotalSeconds ?: template.estimatedDurationSeconds.toInt(),
                hasExplicitGoal = ui.goalTotalSeconds != null,
                onClick = { onOpenGoal(routeKey) },
            )
            RoxZoneCard(
                enabled = template.usesRoxZone,
                onToggle = vm::onRoxZoneToggled,
            )
            CourseList(template)
            Spacer(Modifier.height(40.dp))
        }

        // Footer
        Surface(
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Button(
                    onClick = { template?.let(onStart) },
                    enabled = template != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black,
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text("Start Workout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun TitleBlock(template: WorkoutTemplate) {
    val title = if (template.isBuiltIn) template.division?.displayName ?: template.name else template.name
    val stations = template.segments.count { it.type == SegmentType.Station }
    val runKm = template.segments
        .filter { it.type == SegmentType.Run }
        .mapNotNull { it.distanceMeters }
        .sum() / 1000.0
    val est = (template.estimatedDurationSeconds / 60).toInt()

    Column {
        Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "$stations stations · %.1f km run · ~$est min".format(runKm),
            color = Color(0xFFAAAAAA),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun GoalCard(
    totalSeconds: Int,
    hasExplicitGoal: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("GOALS", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    "GOAL " + formatHms(totalSeconds.toLong() * 1000),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    if (hasExplicitGoal) "Edit segment targets" else "Set a finish time",
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF555555))
        }
    }
}

@Composable
private fun RoxZoneCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("ROX ZONE", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (enabled) "Transitions between Run and Station" else "Direct Run → Station",
                    color = Color(0xFFAAAAAA),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = Color(0xFFFFD700),
                    uncheckedThumbColor = Color(0xFF666666),
                    uncheckedTrackColor = Color(0xFF2C2C2C),
                ),
            )
        }
    }
}

@Composable
private fun CourseList(template: WorkoutTemplate) {
    Text("COURSE", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        template.segments.forEachIndexed { i, seg ->
            val typeColor = when (seg.type) {
                SegmentType.Run -> Color(0xFF007AFF)
                SegmentType.RoxZone -> Color(0xFFFF9500)
                SegmentType.Station -> Color(0xFFFFD700)
            }
            val label = when (seg.type) {
                SegmentType.Run -> "Run ${seg.distanceMeters?.toInt()?.let { it / 1000 }?.let { "${it}km" } ?: ""}".trim()
                SegmentType.RoxZone -> "ROX Zone"
                SegmentType.Station -> seg.stationKind?.displayName ?: "Station"
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                Surface(
                    color = typeColor,
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.width(3.dp).height(18.dp),
                ) {}
                Spacer(Modifier.width(8.dp))
                Text(
                    "%02d".format(i + 1),
                    color = Color(0xFF666666),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(28.dp),
                )
                Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                seg.goalDurationSeconds?.let {
                    Text(
                        formatHms((it * 1000).toLong()),
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

private fun formatHms(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
