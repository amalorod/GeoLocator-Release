package alic.malorodow.geoguessr_app.data.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Definiert die typsicheren Preference-Schlüssel (Keys), unter denen
 * [DailyQuestDataStoreRepository] die lokalen Daily-Quest-Daten eines
 * Gast-Nutzers innerhalb der DataStore-Datei "geoguessr_preferences"
 * ablegt.
 *
 * DataStore erlaubt keinen direkten Zugriff über rohe String-Schlüssel,
 * sondern verlangt dedizierte Key-Objekte (hier: [stringPreferencesKey]),
 * die den erwarteten Werttyp zur Compile-Zeit sicherstellen.
 */
object DailyQuestPreferences {

    /**
     * Speichert die aktuelle Liste aller Daily Quests eines Gast-Nutzers
     * als serialisierten JSON-String (siehe [DailyQuestDataStoreRepository.saveQuests]).
     * JSON wird hier bewusst als einfaches, abhängigkeitsfreies Serialisierungsformat
     * gewählt, da DataStore selbst nur primitive Typen und Strings speichern kann.
     */
    val QUESTS_JSON = stringPreferencesKey("daily_quests_json")

    /**
     * Speichert das Datum (als String) des letzten Daily-Quest-Resets.
     * Wird benötigt, um beim App-Start zu prüfen, ob ein neuer Tag
     * begonnen hat und die Quests entsprechend zurückgesetzt werden müssen.
     */
    val LAST_RESET_DATE = stringPreferencesKey("daily_quest_last_reset")
}