package com.bbdyno.hyroxsim.feature.active

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (ui.finished) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("FINISHED", color = Color(0xFFFFD700), fontSize = 28.sp,
                    fontWeight = FontWeight.Bold)
                Text(formatMs(ui.totalElapsedMs), color = Color.White, fontSize = 36.sp)
                Button(onClick = onFinished) { Text("확인") }
            }
            return@Box
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "${ui.currentSegmentIndex + 1} / ${ui.totalSegments}",
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
            )
            Text(
                text = ui.segmentLabel,
                color = Color(0xFFFFD700),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formatMs(ui.segmentElapsedMs),
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Total ${formatMs(ui.totalElapsedMs)}",
                color = Color(0xFF888888),
                fontSize = 14.sp,
            )
            ui.nextLabel?.let {
                Text(
                    text = "→ $it",
                    color = Color(0xFF666666),
                    fontSize = 12.sp,
                )
            }
            if (ui.engineStateLabel == "paused") {
                Text("PAUSED", color = Color(0xFFFF3B30), fontSize = 14.sp,
                    fontWeight = FontWeight.Bold)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
            ) {
                Button(
                    onClick = vm::onAdvance,
                    enabled = ui.engineStateLabel == "running",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                ) { Text("Next", fontWeight = FontWeight.Bold) }

                if (ui.engineStateLabel == "running") {
                    OutlinedButton(
                        onClick = vm::onPause,
                        modifier = Modifier.weight(1f),
                    ) { Text("Pause") }
                } else if (ui.engineStateLabel == "paused") {
                    OutlinedButton(
                        onClick = vm::onResume,
                        modifier = Modifier.weight(1f),
                    ) { Text("Resume") }
                }
            }
            OutlinedButton(
                onClick = { vm.onEnd(onFinished) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("End workout") }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
