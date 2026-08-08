package com.example.geoguessr_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geoguessr_app.ui.home.HomeScreen
import com.example.geoguessr_app.ui.tutorial.TutorialScreen

/**
 * Zentrale Navigationskomponente der App.
 *
 * Der NavController verwaltet den Back Stack. Die einzelnen Screens
 * erhalten nur Callback-Funktionen und kennen den NavController nicht.
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
                    // Die Spielroute ergänzen wir im nächsten Schritt.
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
    }
}