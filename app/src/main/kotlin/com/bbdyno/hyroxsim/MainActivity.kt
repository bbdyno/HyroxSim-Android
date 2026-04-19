package com.bbdyno.hyroxsim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bbdyno.hyroxsim.feature.active.ActiveWorkoutRoute
import com.bbdyno.hyroxsim.feature.builder.BuilderRoute
import com.bbdyno.hyroxsim.feature.history.HistoryRoute
import com.bbdyno.hyroxsim.feature.home.HomeRoute
import com.bbdyno.hyroxsim.feature.settings.SettingsRoute
import com.bbdyno.hyroxsim.feature.goalsetup.GoalSetupRoute
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
 * Single NavController + push/pop stack — matches iOS UINavigationController
 * UX. Home is root; Builder/History/Settings push on top. Active workout
 * and summary are full-screen pushed destinations.
 */
@Composable
private fun HyroxRootNav() {
    val navController = rememberNavController()
    Scaffold { inner ->
        NavHost(
            navController = navController,
            startDestination = Route.HOME,
            modifier = Modifier.fillMaxSize().padding(inner),
        ) {
            composable(Route.HOME) {
                HomeRoute(
                    onStartDivision = { navController.navigate(Route.activeWorkout(it)) },
                    onStartTemplate = { navController.navigate(Route.activeTemplate(it)) },
                    onOpenBuilder = { navController.navigate(Route.BUILDER) },
                    onOpenHistory = { navController.navigate(Route.HISTORY) },
                    onOpenSettings = { navController.navigate(Route.SETTINGS) },
                    onOpenSummary = { navController.navigate(Route.summary(it)) },
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
            composable(Route.SETTINGS) {
                SettingsRoute(onBack = { navController.popBackStack() })
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
