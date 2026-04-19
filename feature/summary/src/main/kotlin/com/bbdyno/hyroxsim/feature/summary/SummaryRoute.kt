package com.bbdyno.hyroxsim.feature.summary

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
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import com.bbdyno.hyroxsim.core.domain.CompletedWorkout
import com.bbdyno.hyroxsim.core.domain.SegmentRecord
import com.bbdyno.hyroxsim.core.domain.SegmentType

/**
 * Post-workout review, matching iOS `WorkoutSummaryViewController`:
 * header (total + template), per-segment table (split / time / delta),
 * aggregate footer (rox total / run total / avg pace), HR zone chart.
 */
@Composable
fun SummaryRoute(
    workoutId: String,
    onBack: () -> Unit,
    vm: SummaryViewModel = hiltViewModel(),
) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(workoutId) { vm.load(workoutId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        val context = LocalContext.current
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Summary",
                color = Color(0xFFFFD700),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            ui.workout?.let { w ->
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, buildShareText(w))
                    }
                    context.startActivity(Intent.createChooser(intent, "Share"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = "공유", tint = Color(0xFFFFD700))
                }
            }
        }

        if (ui.loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("로딩 중…", color = Color(0xFF888888))
            }
            return@Column
        }
        val w = ui.workout ?: run {
            Text("운동을 찾을 수 없습니다", color = Color(0xFF888888))
            return@Column
        }

        HeaderBlock(workout = w)
        Spacer(Modifier.height(16.dp))
        SegmentTable(workout = w)
        Spacer(Modifier.height(16.dp))
        AggregateFooter(workout = w)
        Spacer(Modifier.height(16.dp))
        HeartRateZoneChart(workout = w)
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun HeaderBlock(workout: CompletedWorkout) {
    Surface(
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("TOTAL", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                formatMs(workout.totalActiveDurationMs),
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                workout.templateName,
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun SegmentTable(workout: CompletedWorkout) {
    Column {
        Text(
            "SPLITS",
            color = Color(0xFFFFD700),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        // Header
        Row(Modifier.padding(vertical = 4.dp)) {
            Text("Split", color = Color(0xFF888888), fontSize = 11.sp, modifier = Modifier.weight(1.6f))
            Text("Time", color = Color(0xFF888888), fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("Δ", color = Color(0xFF888888), fontSize = 11.sp, modifier = Modifier.weight(0.8f))
        }
        HorizontalDivider(color = Color(0xFF222222))
        workout.segments.forEach { seg ->
            SegmentRow(segment = seg, workout = workout)
        }
    }
}

@Composable
private fun SegmentRow(segment: SegmentRecord, workout: CompletedWorkout) {
    val typeColor = when (segment.type) {
        SegmentType.Run -> Color(0xFF007AFF)
        SegmentType.RoxZone -> Color(0xFFFF9500)
        SegmentType.Station -> Color(0xFFFFD700)
    }
    val label = when (segment.type) {
        SegmentType.Run -> "Run"
        SegmentType.RoxZone -> "ROX Zone"
        SegmentType.Station -> workout.resolvedStationDisplayName(segment)
            ?: segment.stationDisplayName ?: "Station"
    }
    val goalSec = segment.goalDurationSeconds ?: 0.0
    val deltaMs = segment.activeDurationMs - (goalSec * 1000).toLong()
    val deltaColor = if (deltaMs > 0) Color(0xFFFF3B30) else Color(0xFFFFD700)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(22.dp)
                .background(typeColor, RoundedCornerShape(2.dp)),
        )
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1.45f))
        Text(
            formatMs(segment.activeDurationMs),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Text(
            formatDeltaMs(deltaMs),
            color = deltaColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.8f),
        )
    }
}

@Composable
private fun AggregateFooter(workout: CompletedWorkout) {
    val runMs = workout.runSegments.sumOf { it.activeDurationMs }
    val roxMs = workout.roxZoneSegments.sumOf { it.activeDurationMs }
    val pace = workout.averageRunPaceSecondsPerKm
    Surface(
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("AGGREGATE", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            StatRow("Run total", formatMs(runMs))
            StatRow("ROX Zone total", formatMs(roxMs))
            pace?.let { StatRow("Avg pace", "${(it / 60).toInt()}:%02d /km".format((it % 60).toInt())) }
            workout.averageHeartRate?.let { StatRow("Avg HR", "$it bpm") }
            workout.maxHeartRate?.let { StatRow("Max HR", "$it bpm") }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color(0xFFAAAAAA), fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HeartRateZoneChart(workout: CompletedWorkout) {
    val allSamples = workout.segments.flatMap { it.measurements.heartRateSamples }
    if (allSamples.isEmpty()) return
    // Rough zone split by BPM thresholds — matches iOS bucketing approx.
    val zones = listOf(
        "Z1" to (allSamples.count { it.bpm < 130 }),
        "Z2" to (allSamples.count { it.bpm in 130..149 }),
        "Z3" to (allSamples.count { it.bpm in 150..164 }),
        "Z4" to (allSamples.count { it.bpm in 165..179 }),
        "Z5" to (allSamples.count { it.bpm >= 180 }),
    )
    val total = zones.sumOf { it.second }.coerceAtLeast(1)
    Column {
        Text("HR ZONES", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        zones.forEach { (name, count) ->
            val pct = count.toFloat() / total
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                Text(name, color = Color(0xFFAAAAAA), fontSize = 11.sp, modifier = Modifier.width(32.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .background(Color(0xFF222222), RoundedCornerShape(4.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(pct)
                            .height(14.dp)
                            .background(zoneColor(name), RoundedCornerShape(4.dp)),
                    )
                }
                Text(
                    "${(pct * 100).toInt()}%",
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .width(36.dp)
                        .padding(start = 6.dp),
                )
            }
        }
    }
}

private fun zoneColor(name: String): Color = when (name) {
    "Z1" -> Color(0xFF4CD964)
    "Z2" -> Color(0xFF5AC8FA)
    "Z3" -> Color(0xFFFFD700)
    "Z4" -> Color(0xFFFF9500)
    else -> Color(0xFFFF3B30)
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun formatDeltaMs(ms: Long): String {
    val sign = if (ms >= 0) "+" else "-"
    val abs = if (ms >= 0) ms else -ms
    return sign + formatMs(abs)
}

private fun buildShareText(workout: CompletedWorkout): String = buildString {
    appendLine("🏆 HyroxSim — ${workout.templateName}")
    appendLine("Total: ${formatMs(workout.totalActiveDurationMs)}")
    workout.averageHeartRate?.let { appendLine("Avg HR: $it bpm") }
    appendLine("Distance: %.2f km".format(workout.totalDistanceMeters / 1000))
    workout.averageRunPaceSecondsPerKm?.let { sec ->
        appendLine("Avg pace: %d:%02d /km".format((sec / 60).toInt(), (sec % 60).toInt()))
    }
}
