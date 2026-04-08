//
//  HomeWearFeature.kt
//  feature-home-wear
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.home.wear

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.bbdyno.hyroxsim.android.core.model.SegmentType
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate

object HomeWearFeatureInfo {
    const val name: String = "feature-home-wear"
}

@Composable
fun HomeWearFeatureScreen(
    templates: List<WorkoutTemplate>,
    pairedLabel: String,
    onOpenHistory: () -> Unit,
    onSelectTemplate: (WorkoutTemplate) -> Unit,
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
                Card(onClick = {}) {
                    Text(pairedLabel)
                }
            }
            items(templates, key = { it.id }) { template ->
                Card(onClick = { onSelectTemplate(template) }) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier,
                    ) {
                        Text(
                            text = template.division?.shortName ?: template.name,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${template.segments.count { it.type == SegmentType.STATION }} stations",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
