package com.example.geoguessr_app.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey

/**
 * Definiert den DataStore-Schlüssel für den Onboarding-Status der App.
 * Analog zu [ThemePreferences] und [ProfilePreferences] werden hier
 * ausschließlich die Key-Konstanten gehalten; Lese-/Schreiblogik liegt
 * in [OnboardingDataStoreRepository].
 */
object OnboardingPreferences {

    // Wird einmalig auf true gesetzt, sobald der Nutzer das Tutorial
    // (regulär oder über den WelcomeScreen) vollständig durchlaufen hat.
    val HAS_SEEN_TUTORIAL =
        booleanPreferencesKey("has_seen_tutorial")
}