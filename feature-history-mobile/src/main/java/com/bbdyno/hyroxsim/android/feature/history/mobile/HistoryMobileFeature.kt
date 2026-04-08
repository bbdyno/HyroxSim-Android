//
//  HistoryMobileFeature.kt
//  feature-history-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.history.mobile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object HistoryMobileFeatureInfo {
    const val name: String = "feature-history-mobile"
}

@Composable
fun HistoryMobileScreen(
    workouts: List<CompletedWorkout>,
    onSelectWorkout: (CompletedWorkout) -> Unit,
) {
    if (workouts.isEmpty()) {
        EmptyHistory()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Workout History", style = MaterialTheme.typography.headlineSmall)
        }

        items(workouts, key = { it.id }) { workout ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectWorkout(workout) },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(workout.templateName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        historyDateFormatter.format(workout.startedAt),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        buildString {
                            append(DurationFormatter.hms(workout.totalDuration))
                            append(" • ")
                            append(DistanceFormatter.short(workout.totalDistanceMeters))
                            workout.averageHeartRate?.let {
                                append(" • ")
                                append("$it bpm")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistory() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Workout History", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Finished workouts will appear here after you end a session on the phone or watch.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private val historyDateFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm")
    .withZone(ZoneId.systemDefault())
