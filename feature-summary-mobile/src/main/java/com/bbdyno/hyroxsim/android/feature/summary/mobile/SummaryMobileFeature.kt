//
//  SummaryMobileFeature.kt
//  feature-summary-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.summary.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bbdyno.hyroxsim.android.core.format.DistanceFormatter
import com.bbdyno.hyroxsim.android.core.format.DurationFormatter
import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.core.model.SegmentRecord

object SummaryMobileFeatureInfo {
    const val name: String = "feature-summary-mobile"
}

@Composable
fun SummaryMobileScreen(
    workout: CompletedWorkout,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(workout.templateName, style = MaterialTheme.typography.headlineSmall)
        }

        item {
            SummaryStats(workout)
        }

        item {
            Text("Segments", style = MaterialTheme.typography.titleMedium)
        }

        items(workout.segments, key = { it.id }) { record ->
            SegmentCard(
                record = record,
                stationName = workout.resolvedStationDisplayName(record),
            )
        }
    }
}

@Composable
private fun SummaryStats(workout: CompletedWorkout) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatLine("Total Time", DurationFormatter.hms(workout.totalDuration))
            StatLine("Active Time", DurationFormatter.hms(workout.totalActiveDuration))
            StatLine("Distance", DistanceFormatter.short(workout.totalDistanceMeters))
            StatLine("Avg Pace", DurationFormatter.pace(workout.averageRunPaceSecondsPerKm))
            StatLine("Avg HR", workout.averageHeartRate?.let { "$it bpm" } ?: "—")
            StatLine("Max HR", workout.maxHeartRate?.let { "$it bpm" } ?: "—")
        }
    }
}

@Composable
private fun SegmentCard(
    record: SegmentRecord,
    stationName: String?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(segmentTitle(record, stationName), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(DurationFormatter.ms(record.activeDuration), style = MaterialTheme.typography.bodySmall)
                Text(DistanceFormatter.short(record.distanceMeters), style = MaterialTheme.typography.bodySmall)
                Text(record.averageHeartRate?.let { "$it bpm" } ?: "—", style = MaterialTheme.typography.bodySmall)
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

private fun segmentTitle(record: SegmentRecord, stationName: String?): String =
    when {
        record.type.name == "RUN" -> "Run ${record.index + 1}"
        record.type.name == "ROX_ZONE" -> "Roxzone"
        !stationName.isNullOrBlank() -> stationName
        else -> "Station"
    }
