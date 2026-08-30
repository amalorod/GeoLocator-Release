package com.example.geoguessr_app.data.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Preference-Schlüssel für die lokalen Daily-Quest-Daten eines Gastes.
 */
object DailyQuestPreferences {
    val QUESTS_JSON = stringPreferencesKey("daily_quests_json")
}
