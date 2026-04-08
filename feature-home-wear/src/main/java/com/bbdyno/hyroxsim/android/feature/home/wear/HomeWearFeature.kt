//
//  HomeWearFeature.kt
//  feature-home-wear
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.home.wear

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate

object HomeWearFeatureInfo {
    const val name: String = "feature-home-wear"
}

@Composable
fun HomeWearFeatureScreen(
    templates: List<WorkoutTemplate>,
    pairedLabel: String,
    onOpenHistory: () -> Unit,
    onStartWorkout: (WorkoutTemplate) -> Unit,
) {
    MaterialTheme {
        ScalingLazyColumn {
            item {
                TimeText()
            }
            item {
                Card(onClick = onOpenHistory) {
                    Text("History")
                }
            }
            item {
                Text(pairedLabel)
            }
            items(templates, key = { it.id }) { template ->
                Button(onClick = { onStartWorkout(template) }) {
                    Text(
                        text = template.name,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
