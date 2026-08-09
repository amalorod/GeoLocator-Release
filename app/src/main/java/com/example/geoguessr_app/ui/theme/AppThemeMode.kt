package com.example.geoguessr_app.ui.theme

enum class AppThemeMode(
    val displayName: String
) {
    LIGHT("Hell"),
    DARK("Dunkel"),
    BEIGE("Beige"),
    BLUE("Blau"),
    ROSE("Rosé");

    fun next(): AppThemeMode {
        val modes = entries
        return modes[(ordinal + 1) % modes.size]
    }
}