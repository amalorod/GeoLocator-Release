package com.example.geoguessr_app.ui.game

/**
 * Konfiguration der verfügbaren Spielmodi.
 *
 * Noch nicht implementierte Modi werden bereits im Auswahlmenü angezeigt,
 * bleiben dort aber deaktiviert.
 */
enum class GameMode(
    val displayName: String,
    val roundDurationSeconds: Int,
    val streetViewNavigationEnabled: Boolean,
    val isAvailable: Boolean
) {
    NORMAL(
        displayName = "Normal",
        roundDurationSeconds = 60,
        streetViewNavigationEnabled = true,
        isAvailable = true
    ),

    PRO(
        displayName = "Pro",
        roundDurationSeconds = 10,
        streetViewNavigationEnabled = false,
        isAvailable = true
    ),

    BATTLE_ROYALE(
        displayName = "Battle Royale",
        roundDurationSeconds = 30,
        streetViewNavigationEnabled = true,
        isAvailable = false
    ),

    MULTIPLAYER(
        displayName = "Multiplayer",
        roundDurationSeconds = 60,
        streetViewNavigationEnabled = true,
        isAvailable = true
    ),

    DAILY_QUEST(
        displayName = "Daily Quest",
        roundDurationSeconds = 60,
        streetViewNavigationEnabled = true,
        isAvailable = true
    ),

    CUSTOM(
        displayName = "Individuell",
        roundDurationSeconds = 60,
        streetViewNavigationEnabled = true,
        isAvailable = true
    )
}