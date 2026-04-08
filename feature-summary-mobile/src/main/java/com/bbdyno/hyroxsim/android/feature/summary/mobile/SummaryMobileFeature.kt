//
//  SummaryMobileFeature.kt
//  feature-summary-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.summary.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bbdyno.hyroxsim.android.core.format.DistanceFormatter
import com.bbdyno.hyroxsim.android.core.format.DurationFormatter
import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.core.model.HeartRateZone
import com.bbdyno.hyroxsim.android.core.model.SegmentRecord
import com.bbdyno.hyroxsim.android.core.model.SegmentType
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxBadge
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxDivider
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxMobileDesign
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxSectionLabel
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxSurfaceCard
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object SummaryMobileFeatureInfo {
    const val name: String = "feature-summary-mobile"
}

private val summaryDateFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm")
    .withZone(ZoneId.systemDefault())

@Composable
fun SummaryMobileScreen(
    workout: CompletedWorkout,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val zoneItems = remember(workout.id) { workout.heartRateZoneDistribution() }
    val runPaces = remember(workout.id) { workout.runPaceItems() }
    val stationItems = remember(workout.id) { workout.stationItems() }
    val breakdownItems = remember(workout.id) { workout.breakdownItems() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 12.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = DurationFormatter.hms(workout.totalDuration),
                    style = HyroxMobileDesign.Typography.LargeNumber,
                    color = HyroxMobileDesign.Colors.TextPrimary,
                )
                Text(
                    text = "TOTAL TIME",
                    style = HyroxMobileDesign.Typography.Label,
                    color = HyroxMobileDesign.Colors.TextTertiary,
                )
                Text(
                    text = workout.division?.displayName ?: workout.templateName,
                    style = MaterialTheme.typography.titleMedium,
                    color = HyroxMobileDesign.Colors.Accent,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = summaryDateFormatter.format(workout.finishedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = HyroxMobileDesign.Colors.TextTertiary,
                )
            }
        }

        item {
            HyroxDivider()
        }

        item {
            SummaryTableHeader()
        }

        items(breakdownItems, key = { it.record.id }) { item ->
            SummarySegmentRow(item = item)
        }

        item {
            HyroxDivider()
        }

        item {
            SummaryStatRow(
                label = "Roxzone Time",
                value = DurationFormatter.hms(workout.roxZoneSegments.sumOf { it.activeDuration }),
                highlighted = true,
            )
        }

        item {
            SummaryStatRow(
                label = "Run Total",
                value = DurationFormatter.hms(workout.runSegments.sumOf { it.activeDuration }),
                highlighted = true,
            )
        }

        item {
            SummaryStatRow(
                label = "Avg Pace",
                value = DurationFormatter.pace(workout.averageRunPaceSecondsPerKm),
            )
        }

        item {
            SummaryStatRow(
                label = "Avg HR",
                value = workout.averageHeartRate?.let { "$it bpm" } ?: "—",
            )
        }

        item {
            SummaryStatRow(
                label = "Max HR",
                value = workout.maxHeartRate?.let { "$it bpm" } ?: "—",
            )
        }

        item {
            HyroxSectionLabel("SUMMARY")
        }

        item {
            HyroxSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                SummaryMetricPair("Active Time", DurationFormatter.hms(workout.totalActiveDuration))
                SummaryMetricPair("Distance", DistanceFormatter.short(workout.totalDistanceMeters))
                SummaryMetricPair("Run Time", DurationFormatter.hms(workout.runSegments.sumOf { it.activeDuration }))
            }
        }

        if (zoneItems.isNotEmpty()) {
            item {
                HyroxSectionLabel("HEART RATE ZONES")
            }

            items(zoneItems, key = { it.zone.name }) { zone ->
                ZoneBreakdownCard(zone)
            }
        }

        if (runPaces.isNotEmpty()) {
            item {
                HyroxSectionLabel("RUN SPLITS")
            }

            items(runPaces, key = { it.index }) { run ->
                DetailMetricCard(
                    label = "Run ${run.index}",
                    value = run.paceText,
                    detail = run.durationText,
                )
            }
        }

        if (stationItems.isNotEmpty()) {
            item {
                HyroxSectionLabel("STATION TIMES")
            }

            items(stationItems, key = { it.index }) { station ->
                DetailMetricCard(
                    label = station.name,
                    value = station.durationText,
                    detail = station.detail,
                )
            }
        }
    }
}

@Composable
private fun SummaryTableHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Split",
            modifier = Modifier.weight(1f),
            style = HyroxMobileDesign.Typography.Section,
            color = HyroxMobileDesign.Colors.Accent,
        )
        Text(
            text = "Time",
            style = HyroxMobileDesign.Typography.Section,
            color = HyroxMobileDesign.Colors.Accent,
        )
    }
}

@Composable
private fun SummarySegmentRow(
    item: BreakdownItem,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item.badge != null) {
            HyroxBadge(
                text = item.badge,
                modifier = Modifier.width(30.dp),
            )
        } else {
            Box(modifier = Modifier.width(30.dp))
        }

        Text(
            text = item.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = HyroxMobileDesign.Colors.TextPrimary.copy(alpha = if (item.dimmed) 0.4f else 1f),
            fontWeight = if (item.badge != null) FontWeight.Bold else FontWeight.Normal,
        )

        Text(
            text = item.durationText,
            style = HyroxMobileDesign.Typography.SmallNumber,
            color = HyroxMobileDesign.Colors.TextPrimary.copy(alpha = if (item.dimmed) 0.4f else 1f),
        )
    }
}

@Composable
private fun SummaryStatRow(
    label: String,
    value: String,
    highlighted: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlighted) HyroxMobileDesign.Colors.Accent else HyroxMobileDesign.Colors.TextSecondary,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium,
        )
        Text(
            text = value,
            style = HyroxMobileDesign.Typography.SmallNumber,
            color = if (highlighted) HyroxMobileDesign.Colors.Accent else HyroxMobileDesign.Colors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SummaryMetricPair(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = HyroxMobileDesign.Colors.TextSecondary)
        Text(value, style = HyroxMobileDesign.Typography.SmallNumber)
    }
}

@Composable
private fun ZoneBreakdownCard(
    item: ZoneSummaryItem,
) {
    HyroxSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${item.zone.label} ${item.zone.description}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(item.durationText, style = HyroxMobileDesign.Typography.SmallNumber)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = HyroxMobileDesign.Colors.SurfaceElevated,
                    shape = MaterialTheme.shapes.small,
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.ratio.toFloat().coerceIn(0.02f, 1f))
                    .background(colorForZone(item.zone), MaterialTheme.shapes.small)
                    .padding(vertical = 6.dp),
            )
        }
        Text(
            text = "${(item.ratio * 100).toInt()}% of active time",
            style = MaterialTheme.typography.bodySmall,
            color = HyroxMobileDesign.Colors.TextSecondary,
        )
    }
}

@Composable
private fun DetailMetricCard(
    label: String,
    value: String,
    detail: String?,
) {
    HyroxSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                detail?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = HyroxMobileDesign.Colors.TextSecondary,
                    )
                }
            }
            Text(
                text = value,
                style = HyroxMobileDesign.Typography.SmallNumber,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private data class ZoneSummaryItem(
    val zone: HeartRateZone,
    val durationText: String,
    val ratio: Double,
)

private data class RunPaceItem(
    val index: Int,
    val paceText: String,
    val durationText: String,
)

private data class StationTimeItem(
    val index: Int,
    val name: String,
    val durationText: String,
    val detail: String?,
)

private data class BreakdownItem(
    val title: String,
    val badge: String?,
    val durationText: String,
    val dimmed: Boolean,
    val record: SegmentRecord,
)

private fun CompletedWorkout.heartRateZoneDistribution(): List<ZoneSummaryItem> {
    val activeDuration = totalActiveDuration.coerceAtLeast(1.0)
    val buckets = LongArray(HeartRateZone.entries.size)
    val samples = segments.flatMap { it.measurements.heartRateSamples }

    samples.zipWithNext { current, next ->
        val delta = (next.timestamp.toEpochMilli() - current.timestamp.toEpochMilli()) / 1000
        if (delta > 0L) {
            zoneForBpm(current.bpm)?.let { zone ->
                buckets[zone.ordinal] += delta
            }
        }
    }

    return HeartRateZone.entries.mapNotNull { zone ->
        val seconds = buckets[zone.ordinal]
        if (seconds <= 0L) {
            null
        } else {
            ZoneSummaryItem(
                zone = zone,
                durationText = DurationFormatter.hms(seconds.toDouble()),
                ratio = seconds.toDouble() / activeDuration.toDouble(),
            )
        }
    }
}

private fun CompletedWorkout.runPaceItems(): List<RunPaceItem> =
    runSegments.mapIndexed { index, record ->
        RunPaceItem(
            index = index + 1,
            paceText = DurationFormatter.pace(record.averagePaceSecondsPerKm),
            durationText = DurationFormatter.hms(record.activeDuration),
        )
    }

private fun CompletedWorkout.stationItems(): List<StationTimeItem> =
    stationSegments.mapIndexed { index, record ->
        StationTimeItem(
            index = index + 1,
            name = resolvedStationDisplayName(record) ?: "Station",
            durationText = DurationFormatter.hms(record.activeDuration),
            detail = null,
        )
    }

private fun CompletedWorkout.breakdownItems(): List<BreakdownItem> {
    var stationNumber = 0
    return segments.map { record ->
        when (record.type) {
            SegmentType.RUN -> BreakdownItem(
                    title = "Running ${segments.takeWhile { it.id != record.id }.count { it.type == SegmentType.RUN } + 1}",
                    badge = null,
                    durationText = DurationFormatter.hms(record.activeDuration),
                dimmed = false,
                record = record,
            )
            SegmentType.ROX_ZONE -> BreakdownItem(
                title = "Rox Zone",
                badge = null,
                durationText = DurationFormatter.hms(record.activeDuration),
                dimmed = true,
                record = record,
            )
            SegmentType.STATION -> {
                stationNumber += 1
                BreakdownItem(
                    title = resolvedStationDisplayName(record) ?: "Station",
                    badge = "%02d".format(stationNumber),
                    durationText = DurationFormatter.hms(record.activeDuration),
                    dimmed = false,
                    record = record,
                )
            }
        }
    }
}

private fun colorForZone(zone: HeartRateZone): Color =
    when (zone) {
        HeartRateZone.Z1 -> Color.LightGray
        HeartRateZone.Z2 -> HyroxMobileDesign.Colors.RunAccent
        HeartRateZone.Z3 -> HyroxMobileDesign.Colors.Success
        HeartRateZone.Z4 -> Color(0xFFFF9500)
        HeartRateZone.Z5 -> HyroxMobileDesign.Colors.Destructive
    }

private fun zoneForBpm(bpm: Int): HeartRateZone =
    when {
        bpm < 120 -> HeartRateZone.Z1
        bpm < 140 -> HeartRateZone.Z2
        bpm < 160 -> HeartRateZone.Z3
        bpm < 180 -> HeartRateZone.Z4
        else -> HeartRateZone.Z5
    }
