//
//  HistoryMobileFeature.kt
//  feature-history-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.history.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bbdyno.hyroxsim.android.core.format.DistanceFormatter
import com.bbdyno.hyroxsim.android.core.format.DurationFormatter
import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxChevron
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxMobileDesign
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxSurfaceCard
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object HistoryMobileFeatureInfo {
    const val name: String = "feature-history-mobile"
}

@Composable
fun HistoryMobileScreen(
    workouts: List<CompletedWorkout>,
    onSelectWorkout: (CompletedWorkout) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    if (workouts.isEmpty()) {
        EmptyHistory(contentPadding = contentPadding)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("History", style = HyroxMobileDesign.Typography.Headline)
        }

        items(workouts, key = { it.id }) { workout ->
            HistoryCard(
                workout = workout,
                onClick = { onSelectWorkout(workout) },
            )
        }
    }
}

@Composable
private fun HistoryCard(
    workout: CompletedWorkout,
    onClick: () -> Unit,
) {
    HyroxSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = workout.division?.shortName ?: workout.templateName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = DurationFormatter.hms(workout.totalDuration),
                    style = HyroxMobileDesign.Typography.MediumNumber,
                    color = HyroxMobileDesign.Colors.Accent,
                )
                Text(
                    text = "${workout.stationSegments.count()} stations · ${DistanceFormatter.short(workout.totalDistanceMeters)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HyroxMobileDesign.Colors.TextSecondary,
                )
                Text(
                    text = historyDateFormatter.format(workout.finishedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = HyroxMobileDesign.Colors.TextTertiary,
                )
            }
            HyroxChevron()
        }
    }
}

@Composable
private fun EmptyHistory(
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 20.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("History", style = HyroxMobileDesign.Typography.Headline)
        Text(
            "No workouts yet. Complete your first HYROX to populate this list.",
            style = MaterialTheme.typography.bodyMedium,
            color = HyroxMobileDesign.Colors.TextSecondary,
        )
    }
}

private val historyDateFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm")
    .withZone(ZoneId.systemDefault())
