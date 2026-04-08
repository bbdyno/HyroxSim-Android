//
//  ConfirmStartWearFeature.kt
//  feature-home-wear
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.home.wear

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import kotlin.math.roundToInt

@Composable
fun ConfirmStartWearScreen(
    template: WorkoutTemplate,
    onStartWorkout: () -> Unit,
) {
    MaterialTheme {
        ScalingLazyColumn {
            item { TimeText() }
            item {
                Text(template.division?.displayName ?: template.name)
            }
            item {
                Card(onClick = {}) {
                    Text(
                        "${template.stationCount} stations • ~${(template.estimatedDurationSeconds / 60.0).roundToInt()} min",
                    )
                }
            }
            item {
                Button(onClick = onStartWorkout) {
                    Text("Start")
                }
            }
        }
    }
}
