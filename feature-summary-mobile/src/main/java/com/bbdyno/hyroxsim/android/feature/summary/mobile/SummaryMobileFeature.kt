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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bbdyno.hyroxsim.android.core.format.DistanceFormatter
import com.bbdyno.hyroxsim.android.core.format.DurationFormatter
import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.core.model.HeartRateZone
import com.bbdyno.hyroxsim.android.core.model.SegmentRecord
import com.bbdyno.hyroxsim.android.core.model.SegmentType
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
) {
    val zoneItems = remember(workout.id) { workout.heartRateZoneDistribution() }
    val runPaces = remember(workout.id) { workout.runPaceItems() }
    val stationItems = remember(workout.id) { workout.stationItems() }
    val breakdownItems = remember(workout.id) { workout.breakdownItems() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    workout.division?.displayName ?: workout.templateName,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    summaryDateFormatter.format(workout.finishedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SummaryStats(workout = workout)
        }

        if (zoneItems.isNotEmpty()) {
            item {
                Text("Heart Rate Zones", style = MaterialTheme.typography.titleMedium)
            }
            items(zoneItems, key = { it.zone.name }) { zone ->
                ZoneBreakdownRow(zone)
            }
        }

        if (runPaces.isNotEmpty()) {
            item {
                Text("Run Splits", style = MaterialTheme.typography.titleMedium)
            }
            items(runPaces, key = { it.index }) { run ->
                MetricDetailCard(
                    label = "Run ${run.index}",
                    value = run.paceText,
                    detail = run.durationText,
                )
            }
        }

        if (stationItems.isNotEmpty()) {
            item {
                Text("Station Times", style = MaterialTheme.typography.titleMedium)
            }
            items(stationItems, key = { it.index }) { station ->
                MetricDetailCard(
                    label = station.name,
                    value = station.durationText,
                    detail = station.detail,
                )
            }
        }

        item {
            Text("Segment Breakdown", style = MaterialTheme.typography.titleMedium)
        }

        items(breakdownItems, key = { it.record.id }) { item ->
            SegmentCard(item = item)
        }
    }
}

@Composable
private fun SummaryStats(
    workout: CompletedWorkout,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatLine("Total Time", DurationFormatter.hms(workout.totalDuration))
            StatLine("Active Time", DurationFormatter.hms(workout.totalActiveDuration))
            StatLine("Run Time", DurationFormatter.hms(workout.runSegments.sumOf { it.activeDuration }))
            StatLine("Rox Zone Time", DurationFormatter.hms(workout.roxZoneSegments.sumOf { it.activeDuration }))
            StatLine("Distance", DistanceFormatter.short(workout.totalDistanceMeters))
            StatLine("Avg Pace", DurationFormatter.pace(workout.averageRunPaceSecondsPerKm))
            StatLine("Avg HR", workout.averageHeartRate?.let { "$it bpm" } ?: "—")
            StatLine("Max HR", workout.maxHeartRate?.let { "$it bpm" } ?: "—")
        }
    }
}

@Composable
private fun ZoneBreakdownRow(
    item: ZoneSummaryItem,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${item.zone.label} ${item.zone.description}",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(item.durationText, style = MaterialTheme.typography.bodySmall)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.ratio.toFloat().coerceIn(0.02f, 1f))
                        .widthIn(min = 20.dp)
                        .background(colorForZone(item.zone), MaterialTheme.shapes.small)
                        .padding(vertical = 6.dp),
                )
            }
            Text(
                "${(item.ratio * 100).toInt()}% of active time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MetricDetailCard(
    label: String,
    value: String,
    detail: String?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                detail?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SegmentCard(
    item: BreakdownItem,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "%02d".format(item.index),
                        color = item.accentColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                }
                Text(item.durationText, style = MaterialTheme.typography.bodyMedium)
            }
            item.detail?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
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
    val index: Int,
    val title: String,
    val detail: String?,
    val durationText: String,
    val accentColor: Color,
    val record: SegmentRecord,
)

private fun CompletedWorkout.heartRateZoneDistribution(maxHeartRate: Int = 190): List<ZoneSummaryItem> {
    var totalSamples = 0
    val counts = mutableMapOf<HeartRateZone, Int>()

    segments.forEach { segment ->
        segment.measurements.heartRateSamples.forEach { sample ->
            val zone = HeartRateZone.zoneForHeartRate(sample.bpm, maxHeartRate)
            counts[zone] = (counts[zone] ?: 0) + 1
            totalSamples += 1
        }
    }

    if (totalSamples == 0 || totalActiveDuration <= 0.0) {
        return emptyList()
    }

    return HeartRateZone.entries.map { zone ->
        val ratio = (counts[zone] ?: 0).toDouble() / totalSamples.toDouble()
        ZoneSummaryItem(
            zone = zone,
            durationText = DurationFormatter.ms(totalActiveDuration * ratio),
            ratio = ratio,
        )
    }
}

private fun CompletedWorkout.runPaceItems(): List<RunPaceItem> =
    runSegments.mapIndexed { index, record ->
        RunPaceItem(
            index = index + 1,
            paceText = DurationFormatter.pace(record.averagePaceSecondsPerKm),
            durationText = DurationFormatter.ms(record.activeDuration),
        )
    }

private fun CompletedWorkout.stationItems(): List<StationTimeItem> =
    stationSegments.mapIndexed { index, record ->
        StationTimeItem(
            index = index + 1,
            name = resolvedStationDisplayName(record) ?: "Station",
            durationText = DurationFormatter.ms(record.activeDuration),
            detail = record.measurements.averageHeartRate?.let { "$it bpm avg" },
        )
    }

private fun CompletedWorkout.breakdownItems(): List<BreakdownItem> =
    segments.mapIndexed { index, record ->
        when (record.type) {
            SegmentType.RUN -> BreakdownItem(
                index = index + 1,
                title = "Running",
                detail = DistanceFormatter.short(record.distanceMeters),
                durationText = DurationFormatter.ms(record.activeDuration),
                accentColor = Color(0xFF5AB2FF),
                record = record,
            )

            SegmentType.ROX_ZONE -> BreakdownItem(
                index = index + 1,
                title = "Rox Zone",
                detail = "Transition",
                durationText = DurationFormatter.ms(record.activeDuration),
                accentColor = Color(0xFFFFB55A),
                record = record,
            )

            SegmentType.STATION -> BreakdownItem(
                index = index + 1,
                title = resolvedStationDisplayName(record) ?: "Station",
                detail = record.measurements.averageHeartRate?.let { "$it bpm avg" },
                durationText = DurationFormatter.ms(record.activeDuration),
                accentColor = Color(0xFFFFD54F),
                record = record,
            )
        }
    }

private fun colorForZone(zone: HeartRateZone): Color =
    when (zone) {
        HeartRateZone.Z1 -> Color(0xFF8D99AE)
        HeartRateZone.Z2 -> Color(0xFF4D96FF)
        HeartRateZone.Z3 -> Color(0xFF59C173)
        HeartRateZone.Z4 -> Color(0xFFFFA552)
        HeartRateZone.Z5 -> Color(0xFFFF5A5F)
    }
