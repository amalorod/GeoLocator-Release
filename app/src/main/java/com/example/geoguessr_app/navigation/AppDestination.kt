package com.example.geoguessr_app.navigation

/**
 * Zentrale Definition sämtlicher Navigationsziele (Routen) der Anwendung.
 *
 * Durch die Kapselung in einer sealed class werden Routennamen an genau
 * einer Stelle im Projekt verwaltet. Das verhindert Tippfehler durch frei
 * geschriebene String-Literale, die in mehreren UI-Dateien wiederholt
 * werden müssten, und erleichtert spätere Umbenennungen von Routen.
 *
 * Jede Route wird als [data object] modelliert, da für die reine
 * Zieldefinition (ohne zusätzliche Instanzdaten) keine eigenständigen
 * Objektinstanzen benötigt werden – ein data object verhält sich dabei
 * wie ein Singleton mit automatisch generierten equals()/toString()-
 * Implementierungen.
 *
 * ARCHITEKTUR-HINWEIS: Aktuell werden Argumente (z. B. lobbyCode,
 * sessionId) manuell als String-Interpolation an die Route angehängt
 * (siehe GeoGuessrNavHost). Seit Navigation-Compose 2.8 empfiehlt Google
 * stattdessen typsichere, serialisierbare Routen-Klassen. Für dieses
 * Projekt wurde aus Zeitgründen bei der klassischen String-basierten
 * Navigation geblieben.
 */
sealed class AppDestination(

    val route: String

) {
    data object Home : AppDestination("home")
    data object Statistics : AppDestination("statistics")
    data object MultiplayerHome : AppDestination("multiplayerHome")
    data object JoinLobby : AppDestination("joinLobby")
    data object MultiplayerLobby : AppDestination("multiplayerLobby")
    data object IndividualSettings : AppDestination("individualSettings")
    data object DailyQuest : AppDestination("dailyQuest")
    data object Profile : AppDestination("profile")

    data object ProfileSetup : AppDestination("profileSetup")
    data object Tutorial : AppDestination("tutorial")
    data object Game : AppDestination("game")
    data object MultiplayerGame : AppDestination("multiplayerGame")
    data object Welcome : AppDestination("welcome")

    // Interne Testrouten zur Entwicklung; nicht über die reguläre
    // Benutzeroberfläche erreichbar, dienen der isolierten Prüfung
    // einzelner Komponenten (Karte, Street View).
    data object MapTest : AppDestination("map_test")
    data object StreetViewTest : AppDestination("street_view_test")
}