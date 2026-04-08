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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText

object HomeWearFeatureInfo {
    const val name: String = "feature-home-wear"
}

@Composable
fun HomeWearFeatureScreen(modules: List<String>) {
    MaterialTheme {
        ScalingLazyColumn {
            item {
                TimeText()
            }
            item {
                Text("HYROX Wear")
            }
            items(modules) { moduleName ->
                Text(
                    text = moduleName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
