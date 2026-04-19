package com.bbdyno.hyroxsim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bbdyno.hyroxsim.feature.active.ActiveWorkoutRoute
import com.bbdyno.hyroxsim.feature.builder.BuilderRoute
import com.bbdyno.hyroxsim.feature.goalsetup.GoalSetupRoute
import com.bbdyno.hyroxsim.feature.history.HistoryRoute
import com.bbdyno.hyroxsim.feature.home.HomeRoute
import com.bbdyno.hyroxsim.feature.home.TemplateDetailRoute
import com.bbdyno.hyroxsim.feature.settings.SettingsRoute
import com.bbdyno.hyroxsim.feature.summary.SummaryRoute
import com.bbdyno.hyroxsim.nav.Route
import com.bbdyno.hyroxsim.ui.theme.HyroxTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HyroxTheme {
                HyroxRootNav()
            }
        }
    }
}

/**
 * Two-tab root (Home / Settings) with push destinations layered on top.
 * iOS-parity: tab bar replaces the old "settings gear" button; pushed
 * screens (Detail, Builder, Goal, Active, Summary, History) hide the bar.
 */
@Composable
private fun HyroxRootNav() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val showBottomBar = currentRoute == Route.HOME || currentRoute == Route.SETTINGS

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color(0xFF0A0A0A)) {
                    listOf(
                        Triple(Route.HOME, "Home", Icons.Default.Home),
                        Triple(Route.SETTINGS, "Settings", Icons.Default.Settings),
                    ).forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color(0xFFFFD700),
                                indicatorColor = Color(0xFFFFD700),
                                unselectedIconColor = Color(0xFF888888),
                                unselectedTextColor = Color(0xFF888888),
                            ),
                            onClick = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = Route.HOME,
            modifier = Modifier.fillMaxSize().padding(inner),
        ) {
            composable(Route.HOME) {
                HomeRoute(
                    onOpenDetail = { navController.navigate(Route.templateDetail(it)) },
                    onOpenBuilder = { navController.navigate(Route.BUILDER) },
                    onOpenHistory = { navController.navigate(Route.HISTORY) },
                    onOpenSummary = { navController.navigate(Route.summary(it)) },
                )
            }
            composable(Route.SETTINGS) {
                SettingsRoute()
            }
            composable(
                route = Route.TEMPLATE_DETAIL,
                arguments = listOf(navArgument("routeKey") { type = NavType.StringType }),
            ) { backStack ->
                val routeKey = backStack.arguments?.getString("routeKey") ?: return@composable
                TemplateDetailRoute(
                    routeKey = routeKey,
                    onBack = { navController.popBackStack() },
                    onStart = { t ->
                        if (t.isBuiltIn) {
                            val raw = t.division?.raw
                            if (raw != null) navController.navigate(Route.activeWorkout(raw))
                        } else {
                            navController.navigate(Route.activeTemplate(t.id))
                        }
                    },
                    onOpenGoal = { navController.navigate(Route.goalSetup(it)) },
                )
            }
            composable(Route.BUILDER) {
                BuilderRoute(onBack = { navController.popBackStack() })
            }
            composable(Route.HISTORY) {
                HistoryRoute(
                    onBack = { navController.popBackStack() },
                    onOpenSummary = { navController.navigate(Route.summary(it)) },
                )
            }
            composable(
                route = Route.ACTIVE_WORKOUT,
                arguments = listOf(navArgument("divisionRaw") { type = NavType.StringType }),
            ) { backStack ->
                val divisionRaw = backStack.arguments?.getString("divisionRaw") ?: return@composable
                ActiveWorkoutRoute(
                    divisionRaw = divisionRaw,
                    onFinished = { navController.popBackStack(Route.HOME, inclusive = false) },
                )
            }
            composable(
                route = Route.ACTIVE_WORKOUT_FROM_TEMPLATE,
                arguments = listOf(navArgument("templateId") { type = NavType.StringType }),
            ) { backStack ->
                val templateId = backStack.arguments?.getString("templateId") ?: return@composable
                ActiveWorkoutRoute(
                    templateId = templateId,
                    onFinished = { navController.popBackStack(Route.HOME, inclusive = false) },
                )
            }
            composable(
                route = Route.GOAL_SETUP,
                arguments = listOf(navArgument("templateId") { type = NavType.StringType }),
            ) { backStack ->
                val templateId = backStack.arguments?.getString("templateId") ?: return@composable
                GoalSetupRoute(
                    templateId = templateId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Route.SUMMARY,
                arguments = listOf(navArgument("workoutId") { type = NavType.StringType }),
            ) { backStack ->
                val workoutId = backStack.arguments?.getString("workoutId") ?: return@composable
                SummaryRoute(
                    workoutId = workoutId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
