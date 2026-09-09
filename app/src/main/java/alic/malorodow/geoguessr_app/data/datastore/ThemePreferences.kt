package alic.malorodow.geoguessr_app.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Definiert die DataStore-Schlüssel für die persistierten Theme-Einstellungen
 * der Anwendung. Analog zu [ProfilePreferences] und [StatisticsPreferences]
 * werden hier ausschließlich die Key-Konstanten gehalten; Lese-/Schreiblogik
 * liegt in [ThemeDataStoreRepository].
 */
object ThemePreferences {

    // Der Enum-Wert von AppThemeMode wird als reiner Name (z. B. "LIGHT")
    // gespeichert, da Preferences DataStore keine Enum-Typen nativ unterstützt.
    val THEME_MODE =
        stringPreferencesKey("theme_mode")

    // Steuert, ob Material-You-Farben (Android 12+, systemabhängig) statt
    // eines der festen Farbschemata verwendet werden sollen.
    val DYNAMIC_COLOR_ENABLED =
        booleanPreferencesKey("dynamic_color_enabled")
}