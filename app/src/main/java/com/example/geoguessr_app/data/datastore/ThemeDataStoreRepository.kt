package com.example.geoguessr_app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.geoguessr_app.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.themeDataStore by preferencesDataStore(name = "theme_settings")

/**
 * Bündelt das aktuell aktive Farbschema als unveränderliches Datenobjekt,
 * damit ViewModel und UI nicht zwei separate Flows (Mode + DynamicColor)
 * einzeln beobachten und synchron halten müssen.
 *
 * @property mode Das vom Nutzer gewählte statische Farbschema.
 * @property dynamicColorEnabled Ob stattdessen die systemseitig aus dem
 * Wallpaper abgeleitete Material-You-Palette verwendet werden soll.
 */
data class ThemeSettings(
    val mode: AppThemeMode = AppThemeMode.LIGHT,
    val dynamicColorEnabled: Boolean = false
)

/**
 * Persistiert und liefert die Theme-Einstellungen der App über Jetpack
 * Preferences DataStore. Bildet innerhalb der Data-Schicht die "Single
 * Source of Truth" für den Theme-Zustand; UI-Layer und ViewModel greifen
 * ausschließlich über [themeSettings] bzw. [setThemeMode]/[setDynamicColorEnabled]
 * darauf zu und halten selbst keinen persistenten Zustand.
 *
 * @param context Application-Context, der über Hilt (@ApplicationContext) injiziert wird.
 */
class ThemeDataStoreRepository(
    private val context: Context
) {

    /**
     * Emittiert bei jeder Änderung die aktuellen [ThemeSettings]. Tritt beim
     * Lesen ein I/O-Fehler auf, wird auf leere Preferences zurückgefallen,
     * damit die App nicht abstürzt, sondern die Default-Werte anzeigt.
     */
    val themeSettings: Flow<ThemeSettings> = context.themeDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val modeName = preferences[ThemePreferences.THEME_MODE]
            val mode = modeName?.let { name ->
                runCatching { AppThemeMode.valueOf(name) }.getOrNull()
            } ?: AppThemeMode.LIGHT

            ThemeSettings(
                mode = mode,
                dynamicColorEnabled = preferences[ThemePreferences.DYNAMIC_COLOR_ENABLED] ?: false
            )
        }

    /**
     * Persistiert das gewählte statische Farbschema. Wird dieses aktiv
     * gesetzt, deaktivieren wir zugleich [ThemePreferences.DYNAMIC_COLOR_ENABLED],
     * da sich beide Modi gegenseitig ausschließen und sonst ein
     * inkonsistenter Zustand (Mode gesetzt, aber dynamicColor aktiv) entstehen könnte.
     */
    suspend fun setThemeMode(mode: AppThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[ThemePreferences.THEME_MODE] = mode.name
            preferences[ThemePreferences.DYNAMIC_COLOR_ENABLED] = false
        }
    }

    /**
     * Aktiviert bzw. deaktiviert die Verwendung der Material-You-Systemfarben.
     */
    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[ThemePreferences.DYNAMIC_COLOR_ENABLED] = enabled
        }
    }
}