package com.example.geoguessr_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geoguessr_app.ui.game.GameRoute
import com.example.geoguessr_app.ui.home.HomeScreen
import com.example.geoguessr_app.ui.maptest.MapTestScreen
import com.example.geoguessr_app.ui.tutorial.TutorialScreen

/**
 * Zentrale Navigationskomponente der App.
 *
 * Der HomeScreen ist wieder das reguläre Startziel. Die isolierte
 * Karten-Testansicht bleibt vorläufig als Route im Projekt erhalten.
 */
@Composable
fun GeoGuessrNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
        modifier = modifier
    ) {
        composable(route = AppDestination.Home.route) {
            HomeScreen(
                onStartGameClick = {
                    navController.navigate(AppDestination.Game.route)
                },
                onTutorialClick = {
                    navController.navigate(AppDestination.Tutorial.route)
                }
            )
        }

        composable(route = AppDestination.Tutorial.route) {
            TutorialScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppDestination.Game.route) {
            GameRoute(
                onExitGame = {
                    navController.popBackStack(
                        route = AppDestination.Home.route,
                        inclusive = false
                    )
                }
            )
        }

        composable(route = AppDestination.MapTest.route) {
            MapTestScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}