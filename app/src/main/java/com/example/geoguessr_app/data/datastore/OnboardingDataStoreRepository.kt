package com.example.geoguessr_app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_settings")

/**
 * Persistiert, ob der Nutzer das Tutorial bereits (z. B. beim ersten
 * App-Start über [WelcomeScreen]) durchlaufen hat. Analog zu
 * [ThemeDataStoreRepository] als "Single Source of Truth" für genau
 * diesen einen Zustand modelliert, damit [GeoGuessrNavHost] beim Start
 * entscheiden kann, ob [WelcomeScreen] oder direkt der Home-Screen
 * gezeigt wird.
 *
 * @param context Application-Context, injiziert über Hilt (@ApplicationContext).
 */
class OnboardingDataStoreRepository(
    private val context: Context
) {

    /**
     * Emittiert `true`, sobald [markTutorialAsSeen] mindestens einmal
     * aufgerufen wurde. Fällt bei einem I/O-Fehler auf `false` zurück
     * (Default: Tutorial noch nicht gesehen), damit im Zweifel eher das
     * Tutorial erneut gezeigt wird, statt die App abstürzen zu lassen.
     */
    val hasSeenTutorial: Flow<Boolean> = context.onboardingDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[OnboardingPreferences.HAS_SEEN_TUTORIAL] ?: false
        }

    /** Markiert das Tutorial dauerhaft als gesehen; wird nie wieder auf false zurückgesetzt. */
    suspend fun markTutorialAsSeen() {
        context.onboardingDataStore.edit { preferences ->
            preferences[OnboardingPreferences.HAS_SEEN_TUTORIAL] = true
        }
    }
}