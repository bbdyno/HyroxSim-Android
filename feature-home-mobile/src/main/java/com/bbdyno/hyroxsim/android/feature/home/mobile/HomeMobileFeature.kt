//
//  HomeMobileFeature.kt
//  feature-home-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.home.mobile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bbdyno.hyroxsim.android.core.format.DistanceFormatter
import com.bbdyno.hyroxsim.android.core.format.DurationFormatter
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate

object HomeMobileFeatureInfo {
    const val name: String = "feature-home-mobile"
}

@Composable
fun HomeMobileScreen(
    templates: List<WorkoutTemplate>,
    pairedLabel: String,
    onOpenBuilder: () -> Unit,
    onOpenHistory: () -> Unit,
    onStartPhoneWorkout: (WorkoutTemplate) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("HYROX Mobile", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Phone-origin workout, template sync, and history live here.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    AssistChip(onClick = {}, label = { Text(pairedLabel) })
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onOpenBuilder) {
                            Text("Builder")
                        }
                        OutlinedButton(onClick = onOpenHistory) {
                            Text("History")
                        }
                    }
                }
            }
        }

        item {
            Text("Templates", style = MaterialTheme.typography.titleLarge)
        }

        items(templates, key = { it.id }) { template ->
            TemplateCard(
                template = template,
                onStartPhoneWorkout = { onStartPhoneWorkout(template) },
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: WorkoutTemplate,
    onStartPhoneWorkout: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStartPhoneWorkout),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(template.name, style = MaterialTheme.typography.titleMedium)
            Text(
                template.division?.displayName ?: "Custom template",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${template.segments.size} segments",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    DistanceFormatter.short(template.totalRunDistanceMeters),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    DurationFormatter.ms(template.estimatedDurationSeconds),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Button(onClick = onStartPhoneWorkout) {
                Text("Start On Phone")
            }
        }
    }
}
