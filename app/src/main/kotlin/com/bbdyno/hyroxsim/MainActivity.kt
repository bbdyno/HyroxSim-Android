package com.bbdyno.hyroxsim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bbdyno.hyroxsim.feature.active.ActiveWorkoutRoute
import com.bbdyno.hyroxsim.feature.builder.BuilderRoute
import com.bbdyno.hyroxsim.feature.history.HistoryRoute
import com.bbdyno.hyroxsim.feature.home.HomeRoute
import com.bbdyno.hyroxsim.feature.settings.SettingsRoute
import com.bbdyno.hyroxsim.ui.theme.HyroxTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HyroxTheme {
                HyroxRootScaffold()
            }
        }
    }
}

private enum class Tab(val label: String) {
    Home("홈"),
    Builder("만들기"),
    History("기록"),
    Settings("설정"),
}

@Composable
private fun HyroxRootScaffold() {
    var tab by remember { mutableStateOf(Tab.Home) }
    var activeTemplateDivision by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            if (activeTemplateDivision == null) {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == Tab.Home,
                        onClick = { tab = Tab.Home },
                        label = { Text(Tab.Home.label) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    )
                    NavigationBarItem(
                        selected = tab == Tab.Builder,
                        onClick = { tab = Tab.Builder },
                        label = { Text(Tab.Builder.label) },
                        icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    )
                    NavigationBarItem(
                        selected = tab == Tab.History,
                        onClick = { tab = Tab.History },
                        label = { Text(Tab.History.label) },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                    )
                    NavigationBarItem(
                        selected = tab == Tab.Settings,
                        onClick = { tab = Tab.Settings },
                        label = { Text(Tab.Settings.label) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    )
                }
            }
        },
    ) { inner ->
        val currentDivision = activeTemplateDivision
        if (currentDivision != null) {
            ActiveWorkoutRoute(
                modifier = Modifier.fillMaxSize().padding(inner),
                divisionRaw = currentDivision,
                onFinished = { activeTemplateDivision = null },
            )
        } else {
            when (tab) {
                Tab.Home -> HomeRoute(
                    modifier = Modifier.fillMaxSize().padding(inner),
                    onStartWorkout = { divisionRaw -> activeTemplateDivision = divisionRaw },
                )
                Tab.Builder -> BuilderRoute(
                    modifier = Modifier.fillMaxSize().padding(inner),
                )
                Tab.History -> HistoryRoute(
                    modifier = Modifier.fillMaxSize().padding(inner),
                )
                Tab.Settings -> SettingsRoute(
                    modifier = Modifier.fillMaxSize().padding(inner),
                )
            }
        }
    }
}
