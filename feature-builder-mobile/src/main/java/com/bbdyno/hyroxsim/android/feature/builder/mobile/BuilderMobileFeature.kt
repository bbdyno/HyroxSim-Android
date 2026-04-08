//
//  BuilderMobileFeature.kt
//  feature-builder-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.builder.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bbdyno.hyroxsim.android.core.model.HyroxDivision

object BuilderMobileFeatureInfo {
    const val name: String = "feature-builder-mobile"
}

@Composable
fun BuilderMobileScreen(
    draftName: String,
    selectedDivision: HyroxDivision,
    onDraftNameChanged: (String) -> Unit,
    onDivisionSelected: (HyroxDivision) -> Unit,
    onSave: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Custom Builder", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            OutlinedTextField(
                value = draftName,
                onValueChange = onDraftNameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Template Name") },
                supportingText = {
                    Text("Creates a renamed HYROX preset that syncs to the watch.")
                },
            )
        }

        item {
            Text("Preset Base", style = MaterialTheme.typography.titleMedium)
        }

        items(HyroxDivision.entries, key = { it.name }) { division ->
            val selected = division == selectedDivision
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(division.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(division.shortName, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { onDivisionSelected(division) }) {
                        Text(if (selected) "Selected" else "Use This Base")
                    }
                }
            }
        }

        item {
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Template")
            }
        }
    }
}
