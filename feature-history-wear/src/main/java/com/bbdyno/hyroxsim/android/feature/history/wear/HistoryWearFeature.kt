//
//  HistoryWearFeature.kt
//  feature-history-wear
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.history.wear

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.bbdyno.hyroxsim.android.core.format.DurationFormatter
import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object HistoryWearFeatureInfo {
    const val name: String = "feature-history-wear"
}

private val wearHistoryDateFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("MM-dd HH:mm")
    .withZone(ZoneId.systemDefault())

@Composable
fun HistoryWearScreen(
    workouts: List<CompletedWorkout>,
    onSelectWorkout: (CompletedWorkout) -> Unit,
) {
    MaterialTheme {
        ScalingLazyColumn {
            item { TimeText() }
            if (workouts.isEmpty()) {
                item {
                    Text("No history yet")
                }
            } else {
                items(workouts, key = { it.id }) { workout ->
                    Card(onClick = { onSelectWorkout(workout) }) {
                        Text(
                            text = "${workout.division?.shortName ?: workout.templateName}\n${DurationFormatter.ms(workout.totalDuration)} • ${wearHistoryDateFormatter.format(workout.finishedAt)}",
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
