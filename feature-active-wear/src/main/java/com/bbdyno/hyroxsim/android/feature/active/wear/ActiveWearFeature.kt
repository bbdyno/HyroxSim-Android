//
//  ActiveWearFeature.kt
//  feature-active-wear
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.active.wear

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.bbdyno.hyroxsim.android.core.sync.LiveWorkoutState

object ActiveWearFeatureInfo {
    const val name: String = "feature-active-wear"
}

@Composable
fun ActiveWearScreen(
    state: LiveWorkoutState,
    onAdvance: () -> Unit,
    onPauseResume: () -> Unit,
    onEnd: () -> Unit,
) {
    MaterialTheme {
        ScalingLazyColumn {
            item { TimeText() }
            item {
                Text(state.segmentLabel)
            }
            state.segmentSubLabel?.let { label ->
                item {
                    Text(label)
                }
            }
            item {
                Card(onClick = {}) {
                    Text("${state.segmentElapsedText}\n${state.totalElapsedText}")
                }
            }
            item {
                Card(onClick = {}) {
                    Text("${state.distanceText}\n${state.heartRateText}")
                }
            }
            item {
                Button(onClick = onAdvance) {
                    Text(if (state.isLastSegment) "Finish" else "Advance")
                }
            }
            item {
                Button(onClick = onPauseResume) {
                    Text(if (state.isPaused) "Resume" else "Pause")
                }
            }
            item {
                Button(onClick = onEnd) {
                    Text("End")
                }
            }
        }
    }
}
