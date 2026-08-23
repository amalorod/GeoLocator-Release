package com.example.geoguessr_app.navigation

/**
 * Enthält alle Navigationsziele der App.
 *
 * Zentrale Routen vermeiden frei geschriebene Routennamen
 * in verschiedenen UI-Dateien.
 */
sealed class AppDestination(

val route: String

) { data object Home : AppDestination("home")

    data object Statistics : AppDestination("statistics")

    data object MultiplayerLobby : AppDestination  ("multiplayerLobby")
    data object Profile : AppDestination("profile")
    data object Tutorial : AppDestination("tutorial")

    data object Game : AppDestination("game")

    data object MapTest : AppDestination("map_test")

    data object StreetViewTest : AppDestination("street_view_test")

}