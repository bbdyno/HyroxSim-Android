//
//  MainActivity.kt
//  app-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bbdyno.hyroxsim.android.core.engine.CoreEngineModuleInfo
import com.bbdyno.hyroxsim.android.core.format.CoreFormatModuleInfo
import com.bbdyno.hyroxsim.android.core.model.CoreModelModuleInfo
import com.bbdyno.hyroxsim.android.core.sync.CoreSyncModuleInfo
import com.bbdyno.hyroxsim.android.data.datalayer.DataLayerModuleInfo
import com.bbdyno.hyroxsim.android.data.healthconnect.HealthConnectModuleInfo
import com.bbdyno.hyroxsim.android.data.local.LocalDataModuleInfo
import com.bbdyno.hyroxsim.android.feature.active.mobile.ActiveMobileFeatureInfo
import com.bbdyno.hyroxsim.android.feature.builder.mobile.BuilderMobileFeatureInfo
import com.bbdyno.hyroxsim.android.feature.history.mobile.HistoryMobileFeatureInfo
import com.bbdyno.hyroxsim.android.feature.home.mobile.HomeMobileFeatureCard
import com.bbdyno.hyroxsim.android.feature.home.mobile.HomeMobileFeatureInfo
import com.bbdyno.hyroxsim.android.feature.summary.mobile.SummaryMobileFeatureInfo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MobileApp()
            }
        }
    }
}

@Composable
private fun MobileApp() {
    val phaseZeroModules = listOf(
        CoreModelModuleInfo.name,
        CoreEngineModuleInfo.name,
        CoreFormatModuleInfo.name,
        CoreSyncModuleInfo.name,
        LocalDataModuleInfo.name,
        DataLayerModuleInfo.name,
        HealthConnectModuleInfo.name,
        HomeMobileFeatureInfo.name,
        BuilderMobileFeatureInfo.name,
        ActiveMobileFeatureInfo.name,
        HistoryMobileFeatureInfo.name,
        SummaryMobileFeatureInfo.name,
    )

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                HomeMobileFeatureCard()
            }
            items(phaseZeroModules) { moduleName ->
                ModuleRow(moduleName)
            }
        }
    }
}

@Composable
private fun ModuleRow(moduleName: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = moduleName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Phase 0 scaffold ready",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
