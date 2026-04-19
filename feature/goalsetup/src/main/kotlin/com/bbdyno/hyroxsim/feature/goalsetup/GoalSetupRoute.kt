package com.bbdyno.hyroxsim.feature.goalsetup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.bbdyno.hyroxsim.core.domain.SegmentType

/**
 * Goal setup: pick total target time for a template, preview per-segment
 * split, save locally + push to the Garmin watch.
 *
 * Mirrors iOS `PacePlannerViewController`: slider for total time, live
 * per-segment projection, "Save" commits both local and Garmin state.
 */
@Composable
fun GoalSetupRoute(
    templateId: String,
    onBack: () -> Unit,
    vm: GoalSetupViewModel = hiltViewModel(),
) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(templateId) { vm.load(templateId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text("Goal", color = Color(0xFFFFD700), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        val template = ui.template
        if (template == null) {
            Text("로딩 중…", color = Color(0xFF888888))
            return@Column
        }

        Surface(
            color = Color(0xFF0C0C0C),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("TOTAL TARGET", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                    formatMs(ui.totalSeconds * 1000L),
                    color = Color.White,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    template.name,
                    color = Color(0xFFAAAAAA),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = ui.totalSeconds.toFloat(),
                    onValueChange = { vm.onTotalChanged(it.toInt()) },
                    valueRange = (30 * 60).toFloat()..(150 * 60).toFloat(),
                    steps = 119,    // 1-min increments
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFFD700),
                        activeTrackColor = Color(0xFFFFD700),
                        inactiveTrackColor = Color(0xFF333333),
                    ),
                )
                Row {
                    Text("30:00", color = Color(0xFF666666), fontSize = 11.sp)
                    Spacer(Modifier.weight(1f))
                    Text("2:30:00", color = Color(0xFF666666), fontSize = 11.sp)
                }
            }
        }

        Text(
            "PER-SEGMENT PROJECTION",
            color = Color(0xFFFFD700),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ui.perSegmentSeconds.take(10).forEachIndexed { i, sec ->
                val seg = template.segments.getOrNull(i) ?: return@forEachIndexed
                val typeColor = when (seg.type) {
                    SegmentType.Run -> Color(0xFF007AFF)
                    SegmentType.RoxZone -> Color(0xFFFF9500)
                    SegmentType.Station -> Color(0xFFFFD700)
                }
                val label = when (seg.type) {
                    SegmentType.Run -> "Run"
                    SegmentType.RoxZone -> "ROX"
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
                    Text(label, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text(
                        formatMs(sec * 1000L),
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            if (ui.perSegmentSeconds.size > 10) {
                Text(
                    "+${ui.perSegmentSeconds.size - 10} more segments",
                    color = Color(0xFF555555),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        HorizontalDivider(color = Color(0xFF222222))

        Button(
            onClick = { vm.onSave(onDone = onBack) },
            enabled = !ui.saving,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (ui.saving) "저장 중…" else "Save + Sync to Garmin",
                fontWeight = FontWeight.Bold,
            )
        }

        ui.message?.let {
            Text(it, color = Color(0xFFAAAAAA), fontSize = 12.sp)
        }

        Spacer(Modifier.height(40.dp))
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
