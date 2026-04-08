//
//  SummaryWearFeature.kt
//  feature-summary-wear
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.summary.wear

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.bbdyno.hyroxsim.android.core.format.DistanceFormatter
import com.bbdyno.hyroxsim.android.core.format.DurationFormatter
import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.core.model.SegmentType

object SummaryWearFeatureInfo {
    const val name: String = "feature-summary-wear"
}

@Composable
fun SummaryWearScreen(workout: CompletedWorkout) {
    MaterialTheme {
        ScalingLazyColumn {
            item { TimeText() }
            item {
                Text(workout.division?.shortName ?: workout.templateName)
            }
            item {
                Card(onClick = {}) {
                    Text(
                        "${DurationFormatter.ms(workout.totalDuration)} • ${DistanceFormatter.short(workout.totalDistanceMeters)}",
                    )
                }
            }
            item {
                Card(onClick = {}) {
                    Text(
                        "Avg HR ${workout.averageHeartRate?.toString() ?: "—"}\nMax HR ${workout.maxHeartRate?.toString() ?: "—"}",
                    )
                }
            }
            items(workout.segments, key = { it.id }) { record ->
                Card(onClick = {}) {
                    Text(
                        "${wearSegmentLabel(workout, record)}\n${DurationFormatter.ms(record.activeDuration)}",
                    )
                }
            }
        }
    }
}

private fun wearSegmentLabel(
    workout: CompletedWorkout,
    record: com.bbdyno.hyroxsim.android.core.model.SegmentRecord,
): String =
    when (record.type) {
        SegmentType.RUN -> "Running"
        SegmentType.ROX_ZONE -> "Rox Zone"
        SegmentType.STATION -> workout.resolvedStationDisplayName(record) ?: "Station"
    }
