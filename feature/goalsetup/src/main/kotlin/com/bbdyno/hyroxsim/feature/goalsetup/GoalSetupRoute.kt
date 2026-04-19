package com.bbdyno.hyroxsim.feature.goalsetup

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
import com.bbdyno.hyroxsim.core.domain.PacePlan
import com.bbdyno.hyroxsim.core.domain.SegmentType
import com.bbdyno.hyroxsim.core.domain.StationKind

/**
 * Goal setup — data-driven pace planner matching iOS `PacePlannerViewController`.
 *
 * Shows (division-dependent):
 * - Tier badge + percentile derived from real race data (~200 events)
 * - Total target slider
 * - Per-segment projection (runs use adaptive fatigue curve, stations use
 *   per-kind averages from the chosen bucket)
 * - Save button that pushes to Garmin when a watch is paired
 */
@Composable
fun GoalSetupRoute(
    templateId: String,
    onBack: () -> Unit,
    vm: GoalSetupViewModel = hiltViewModel(),
) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(templateId) {
        vm.load(templateId)
        vm.refreshPairing()
    }

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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text("Goal", color = Color(0xFFFFD700), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        val template = ui.template
        if (template == null) {
            Text("Loading…", color = Color(0xFF888888))
            return@Column
        }

        // TIER / PERCENTILE (only when plan is available — requires division)
        ui.plan?.let { plan ->
            TierBadge(tier = ui.tier ?: "—", plan = plan)
        }

        // TOTAL TARGET + slider
        Surface(
            color = Color(0xFF0C0C0C),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "GOAL FINISH TIME",
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    formatHms(ui.totalSeconds.toLong() * 1000),
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

        // PER-STATION PROJECTION (data-driven averages from bucket)
        ui.plan?.let { plan ->
            Text(
                "PER-STATION (avg from bucket)",
                color = Color(0xFFFFD700),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (kind in StationKind.standardOrder) {
                    val sec = plan.stationTimes[kind.raw] ?: continue
                    StationRow(name = kind.displayName, sec = sec)
                }
            }
            HorizontalDivider(color = Color(0xFF222222))
            PaceFooter(plan = plan)
        }

        // PER-SEGMENT PROJECTION (runs + rox + stations mapped onto template)
        Text(
            "PER-SEGMENT PROJECTION",
            color = Color(0xFFFFD700),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ui.perSegmentSeconds.take(12).forEachIndexed { i, sec ->
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
                        formatMs(sec.toLong() * 1000),
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            if (ui.perSegmentSeconds.size > 12) {
                Text(
                    "+${ui.perSegmentSeconds.size - 12} more segments",
                    color = Color(0xFF555555),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        HorizontalDivider(color = Color(0xFF222222))

        // PAIRING STATUS HINT
        Text(
            if (ui.isPaired) "Watch connected — goal will sync on save"
            else "Watch not paired — saving locally only",
            color = if (ui.isPaired) Color(0xFF30D158) else Color(0xFF888888),
            fontSize = 11.sp,
        )

        Button(
            onClick = { vm.onSave(onDone = onBack) },
            enabled = !ui.saving,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            val label = when {
                ui.saving -> "Saving…"
                ui.isPaired -> "Save + Sync to Garmin"
                else -> "Save"
            }
            Text(label, fontWeight = FontWeight.Bold)
        }

        ui.message?.let {
            Text(it, color = Color(0xFFAAAAAA), fontSize = 12.sp)
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun TierBadge(tier: String, plan: PacePlan) {
    Surface(
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(tierColor(tier), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(tier, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Top ${"%.1f".format(plan.percentile)}%",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "of ${formatThousands(plan.totalAthletes)} athletes",
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun StationRow(name: String, sec: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
    ) {
        Text(name, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(
            formatMs(sec.toLong() * 1000),
            color = Color(0xFFAAAAAA),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PaceFooter(plan: PacePlan) {
    // paceSeconds87 = seconds per 1km (averaged over the 8.7km run distance).
    val perKm = plan.paceSeconds87
    val kmMin = perKm / 60
    val kmSec = perKm % 60
    val totalRun = (perKm * 8.7).toInt()
    val rMin = totalRun / 60
    val rSec = totalRun % 60
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("RUN PACE", color = Color(0xFF888888), fontSize = 10.sp)
            Text(
                "%d:%02d /km".format(kmMin, kmSec),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("RUN TOTAL (8.7km)", color = Color(0xFF888888), fontSize = 10.sp)
            Text(
                "%d:%02d".format(rMin, rSec),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun tierColor(tier: String): Color = when (tier) {
    "APEX" -> Color(0xFFFFD700)
    "PRO" -> Color(0xFFFF9500)
    "EXPERT" -> Color(0xFFFF453A)
    "STRONG" -> Color(0xFFBF5AF2)
    "SOLID" -> Color(0xFF0A84FF)
    "STEADY" -> Color(0xFF64D2FF)
    "RISING" -> Color(0xFF32D74B)
    else -> Color(0xFF8E8E93)
}

private fun formatHms(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun formatMs(ms: Long): String = formatHms(ms)

private fun formatThousands(n: Int): String =
    "%,d".format(n)
