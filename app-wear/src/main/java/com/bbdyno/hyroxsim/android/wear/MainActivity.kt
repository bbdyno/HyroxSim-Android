//
//  MainActivity.kt
//  app-wear
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.bbdyno.hyroxsim.android.core.engine.CoreEngineModuleInfo
import com.bbdyno.hyroxsim.android.core.format.CoreFormatModuleInfo
import com.bbdyno.hyroxsim.android.core.model.CoreModelModuleInfo
import com.bbdyno.hyroxsim.android.core.sync.CoreSyncModuleInfo
import com.bbdyno.hyroxsim.android.data.datalayer.DataLayerModuleInfo
import com.bbdyno.hyroxsim.android.data.healthservices.HealthServicesModuleInfo
import com.bbdyno.hyroxsim.android.data.local.LocalDataModuleInfo
import com.bbdyno.hyroxsim.android.feature.active.wear.ActiveWearFeatureInfo
import com.bbdyno.hyroxsim.android.feature.history.wear.HistoryWearFeatureInfo
import com.bbdyno.hyroxsim.android.feature.home.wear.HomeWearFeatureScreen
import com.bbdyno.hyroxsim.android.feature.home.wear.HomeWearFeatureInfo
import com.bbdyno.hyroxsim.android.feature.summary.wear.SummaryWearFeatureInfo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
private fun WearApp() {
    val modules = listOf(
        CoreModelModuleInfo.name,
        CoreEngineModuleInfo.name,
        CoreFormatModuleInfo.name,
        CoreSyncModuleInfo.name,
        LocalDataModuleInfo.name,
        DataLayerModuleInfo.name,
        HealthServicesModuleInfo.name,
        HomeWearFeatureInfo.name,
        ActiveWearFeatureInfo.name,
        HistoryWearFeatureInfo.name,
        SummaryWearFeatureInfo.name,
    )
    HomeWearFeatureScreen(modules = modules)
}
