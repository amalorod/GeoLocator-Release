package com.example.geoguessr_app.navigation

/**
 * Enthält alle Navigationsziele der App.
 *
 * Die Routen werden zentral definiert, damit keine frei geschriebenen
 * Strings über verschiedene Screens verteilt werden.
 */
sealed class AppDestination(val route: String) {

    data object Home : AppDestination("home")

    data object Tutorial : AppDestination("tutorial")
}