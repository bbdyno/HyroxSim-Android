package com.bbdyno.hyroxsim.feature.active

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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

/**
 * Active workout matching iOS `ActiveWorkoutViewController`:
 * segment label + counter, large mono timer, goal card with deltas,
 * controls row (pause / advance / end).
 */
@Composable
fun ActiveWorkoutRoute(
    modifier: Modifier = Modifier,
    divisionRaw: String? = null,
    templateId: String? = null,
    onFinished: () -> Unit,
    vm: ActiveWorkoutViewModel = hiltViewModel(),
) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(divisionRaw, templateId) {
        when {
            templateId != null -> vm.startForTemplate(templateId)
            divisionRaw != null -> vm.startForDivision(divisionRaw)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (ui.finished) {
            FinishedView(totalMs = ui.totalElapsedMs, onOk = onFinished)
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "${ui.currentSegmentIndex + 1} / ${ui.totalSegments}",
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
            )
            Text(
                ui.segmentLabel,
                color = Color(0xFFFFD700),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                formatMs(ui.segmentElapsedMs),
                color = Color.White,
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Text(
                "TOTAL ${formatMs(ui.totalElapsedMs)}",
                color = Color(0xFF888888),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )

            GoalCard(ui = ui)

            ui.nextLabel?.let {
                Text(
                    "→ $it",
                    color = Color(0xFF666666),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            if (ui.engineStateLabel == "paused") {
                Text(
                    "PAUSED",
                    color = Color(0xFFFF3B30),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.weight(1f))

            ControlsRow(
                engineStateLabel = ui.engineStateLabel,
                onAdvance = vm::onAdvance,
                onPause = vm::onPause,
                onResume = vm::onResume,
                onEnd = { vm.onEnd { onFinished() } },
            )
        }
    }
}

@Composable
private fun GoalCard(ui: ActiveWorkoutUiState) {
    Surface(
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GoalRow(label = "SEG", target = ui.segmentTargetMs, deltaMs = ui.segmentDeltaMs)
            GoalRow(label = "TOTAL", target = ui.totalTargetMs, deltaMs = ui.totalDeltaMs)
        }
    }
}

@Composable
private fun GoalRow(label: String, target: Long, deltaMs: Long) {
    val deltaColor = if (deltaMs > 0) Color(0xFFFF3B30) else Color(0xFFFFD700)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            color = Color(0xFFFFD700),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(42.dp),
        )
        Text(
            "target ${formatMs(target)}",
            color = Color(0xFFAAAAAA),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            formatDeltaMs(deltaMs),
            color = deltaColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ControlsRow(
    engineStateLabel: String,
    onAdvance: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onEnd: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        CircleActionButton(
            label = if (engineStateLabel == "paused") "▶" else "⏸",
            onClick = { if (engineStateLabel == "paused") onResume() else onPause() },
            color = Color(0xFF222222),
            contentColor = Color.White,
        )

        Button(
            onClick = onAdvance,
            enabled = engineStateLabel == "running",
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black,
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
        ) {
            Text("NEXT →", fontWeight = FontWeight.Bold)
        }

        CircleActionButton(
            label = "■",
            onClick = onEnd,
            color = Color(0xFFFF3B30),
            contentColor = Color.White,
        )
    }
}

@Composable
private fun CircleActionButton(
    label: String,
    onClick: () -> Unit,
    color: Color,
    contentColor: Color,
) {
    Surface(
        onClick = onClick,
        color = color,
        contentColor = contentColor,
        shape = CircleShape,
        modifier = Modifier.size(56.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = contentColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FinishedView(totalMs: Long, onOk: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("FINISHED", color = Color(0xFFFFD700), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            formatMs(totalMs),
            color = Color.White,
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        OutlinedButton(onClick = onOk) { Text("Done") }
    }
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
