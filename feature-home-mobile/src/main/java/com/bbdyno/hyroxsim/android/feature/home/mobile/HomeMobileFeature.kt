//
//  HomeMobileFeature.kt
//  feature-home-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.home.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object HomeMobileFeatureInfo {
    const val name: String = "feature-home-mobile"
}

@Composable
fun HomeMobileFeatureCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("HYROX Mobile", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Phase 0 shell for presets, builder, history, and live workout flow.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
