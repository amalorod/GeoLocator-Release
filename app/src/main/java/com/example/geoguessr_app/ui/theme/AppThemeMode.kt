package com.example.geoguessr_app.ui.theme

/**
 * Repräsentiert die vom Nutzer wählbaren Farbschemata der Anwendung.
 *
 * Jeder Wert kapselt neben dem internen Enum-Namen einen für die UI
 * bestimmten [displayName], der in den Einstellungen (z. B. im Profil-
 * oder Optionsmenü) angezeigt wird, damit keine technischen Bezeichner
 * (LIGHT, DARK, ...) direkt im UI erscheinen.
 *
 * @property displayName Lesbarer, deutschsprachiger Anzeigename für die UI.
 */
enum class AppThemeMode(
    val displayName: String
) {
    LIGHT("Hell"),
    DARK("Dunkel"),
    BEIGE("Beige"),
    BLUE("Blau"),
    ROSE("Rosé");

}