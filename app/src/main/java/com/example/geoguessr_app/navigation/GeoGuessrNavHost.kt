package com.example.geoguessr_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geoguessr_app.ui.game.GameRoute
import com.example.geoguessr_app.ui.game.GameViewModel
import com.example.geoguessr_app.ui.game.GameMode
import com.example.geoguessr_app.ui.home.HomeScreen
import com.example.geoguessr_app.ui.maptest.MapTestScreen
import com.example.geoguessr_app.ui.streetviewtest.StreetViewTestScreen
import com.example.geoguessr_app.ui.theme.AppThemeMode
import com.example.geoguessr_app.ui.tutorial.TutorialScreen
import com.example.geoguessr_app.data.statistics.StatisticsRepository
import com.example.geoguessr_app.ui.statistics.LifetimeStatisticsScreen
import com.example.geoguessr_app.data.profile.ProfileRepository
import com.example.geoguessr_app.ui.profile.ProfileScreen

/**
 * Zentrale Navigation der App.
 *
 * Das GameViewModel wird oberhalb der einzelnen Ziele erzeugt.
 * Dadurch bleibt die aktive Partie beim Wechsel zum Hauptmenü erhalten.
 */
@Composable
fun GeoGuessrNavHost(

    selectedGameMode: GameMode,
    onGameModeSelected: (GameMode) -> Unit,
    modifier: Modifier = Modifier,
    currentThemeName: String,
    onExitAppClick: () -> Unit,
    currentTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
) {
    val navController = rememberNavController()
    val gameViewModel: GameViewModel = hiltViewModel()

    var isGameInBackground by rememberSaveable {
        mutableStateOf(false)
    }

    NavHost(

        navController = navController,
        startDestination = AppDestination.Home.route,
        modifier = modifier,
    ) {
        composable(route = AppDestination.Home.route) {
            HomeScreen(
                currentThemeName = currentThemeName,
                onProfileClick = {
                    navController.navigate(
                        AppDestination.Profile.route
                    )
                },
                onStatisticsClick = {
                    navController.navigate(
                        AppDestination.Statistics.route
                    )
                },
                onExitAppClick = onExitAppClick,
                onThemeClick = { onThemeSelected(currentTheme.next()) },
                hasActiveGame = isGameInBackground,
                onStartGameClick = {
                    isGameInBackground = false
                    gameViewModel.startNewGame(selectedGameMode)

                    navController.navigate(AppDestination.Game.route) {
                        launchSingleTop = true
                    }
                },


                onResumeGameClick = {
                    gameViewModel.resumeGame()
                    isGameInBackground = false

                    /*
                     * Entfernt das vorübergehend geöffnete Hauptmenü
                     * und zeigt wieder denselben Game-Eintrag.
                     */
                    navController.popBackStack()
                },
                onTutorialClick = {
                    navController.navigate(AppDestination.Tutorial.route)
                },
                selectedGameMode = selectedGameMode,
                onGameModeSelected = onGameModeSelected,
            )
        }

        composable(
            route = AppDestination.Profile.route
        ) {

            val profile =
                ProfileRepository.profile
                    .collectAsState()

            ProfileScreen(
                profile = profile.value,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppDestination.Tutorial.route) {
            TutorialScreen(
                onBackClick = navController::popBackStack
            )
        }

        composable(
            route = AppDestination.Statistics.route
        ) {

            val statistics =
                StatisticsRepository
                    .statistics
                    .collectAsState()

            LifetimeStatisticsScreen(
                statistics = statistics.value,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppDestination.Game.route) {
            GameRoute(
                currentThemeName = currentThemeName,
                currentTheme = currentTheme,
                onThemeSelected = onThemeSelected,
                viewModel = gameViewModel,
                onHomeClick = {
                    isGameInBackground = true

                    navController.navigate(AppDestination.Home.route) {
                        launchSingleTop = true
                    }
                },
                onExitGame = {
                    isGameInBackground = false

                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AppDestination.MapTest.route) {
            MapTestScreen(
                onBackClick = navController::popBackStack
            )
        }

        composable(route = AppDestination.StreetViewTest.route) {
            StreetViewTestScreen(
                onBackClick = navController::popBackStack
            )
        }
    }
}
