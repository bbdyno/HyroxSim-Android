//
//  ActiveMobileFeature.kt
//  feature-active-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.active.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bbdyno.hyroxsim.android.core.sync.LiveWorkoutState

object ActiveMobileFeatureInfo {
    const val name: String = "feature-active-mobile"
}

@Composable
fun ActiveMobileScreen(
    state: LiveWorkoutState,
    modeLabel: String,
    onAdvance: () -> Unit,
    onPauseResume: () -> Unit,
    onEnd: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(state.templateName, style = MaterialTheme.typography.headlineSmall)
                    Text(modeLabel, style = MaterialTheme.typography.bodySmall)
                    Text(state.segmentLabel, style = MaterialTheme.typography.titleLarge)
                    state.segmentSubLabel?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        items(
            listOf(
                "Segment Time" to state.segmentElapsedText,
                "Total Time" to state.totalElapsedText,
                "Distance" to state.distanceText,
                "Pace" to state.paceText,
                "Heart Rate" to state.heartRateText,
                "Station" to (state.stationNameText ?: "—"),
                "Target" to (state.stationTargetText ?: "—"),
            ),
        ) { row ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(row.first, style = MaterialTheme.typography.bodySmall)
                    Text(row.second, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(onClick = onAdvance, enabled = !state.isFinished) {
                    Text(if (state.isLastSegment) "Finish" else "Advance")
                }
                OutlinedButton(onClick = onPauseResume, enabled = !state.isFinished) {
                    Text(if (state.isPaused) "Resume" else "Pause")
                }
                OutlinedButton(onClick = onEnd) {
                    Text("End")
                }
            }
        }
    }
}
