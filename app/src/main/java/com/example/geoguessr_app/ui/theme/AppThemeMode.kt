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

    /**
     * Liefert das nächste Theme in der Enum-Reihenfolge und springt bei
     * Erreichen des letzten Eintrags wieder zum ersten zurück (zyklisches
     * Durchschalten, z. B. für einen "Theme wechseln"-Button).
     *
     * Verwendung von [entries] statt [values] vermeidet die bei jedem
     * Aufruf neu erzeugte Array-Kopie von `values()` und ist damit die
     * empfängerseitig effizientere, seit Kotlin 1.9 empfohlene Variante.
     *
     * @return Das im Enum-Deklarationsring nachfolgende [AppThemeMode].
     */
    fun next(): AppThemeMode {
        val modes = entries
        return modes[(ordinal + 1) % modes.size]
    }
}