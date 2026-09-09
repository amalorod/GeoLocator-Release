package alic.malorodow.geoguessr_app.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import alic.malorodow.geoguessr_app.domain.model.dailyquest.DailyQuest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistiert den Daily-Quest-Fortschritt eines Gast-Nutzers lokal
 * über Jetpack DataStore, analog zu StatisticsDataStoreRepository.
 *
 * Wird ausschließlich für Gast-Nutzer verwendet, die kein Firebase-
 * Profil besitzen. Für angemeldete Nutzer ist DailyQuestRepository
 * über Firebase Realtime Database die Single Source of Truth.
 *
 * Architektonisch gehört diese Klasse zur Data-Layer im Sinne von
 * Clean Architecture: Sie kapselt den konkreten Persistenz-Mechanismus
 * (DataStore + JSON-Serialisierung) und wird über Hilt als Singleton
 * bereitgestellt (siehe [DataStoreModule]), sodass darüberliegende
 * Repository- bzw. ViewModel-Schichten nicht wissen müssen, WIE die
 * Daten gespeichert werden.
 *
 * @param context Application-Context, über den auf die gemeinsame
 * [dataStore]-Instanz aus AppDataStore.kt zugegriffen wird. Wird per
 * Hilt-@Provides-Methode injiziert, nicht über einen @Inject-Konstruktor,
 * da die Bereitstellung zentral im DataStoreModule erfolgt.
 */
class DailyQuestDataStoreRepository(
    private val context: Context
) {

    /**
     * Serialisiert die übergebene Liste an [DailyQuest]-Objekten manuell
     * als JSON-Array und schreibt sie asynchron (suspend) in den DataStore.
     *
     * Die manuelle JSON-Konvertierung (statt einer Serialisierungs-
     * Bibliothek wie kotlinx.serialization) wurde bewusst gewählt, um
     * ohne zusätzliche Abhängigkeit auszukommen, da DataStore
     * ausschließlich primitive Typen bzw. Strings persistieren kann.
     *
     * Tritt ein Fehler auf (z. B. I/O-Problem), wird dieser geloggt statt
     * eine Exception nach außen zu werfen, damit ein einzelner
     * fehlgeschlagener Speichervorgang nicht die gesamte App abstürzen lässt.
     *
     * @param quests Die vollständige, aktuelle Liste der Daily Quests,
     * die den bisherigen Zustand in den Preferences vollständig überschreibt.
     */
    suspend fun saveQuests(quests: List<DailyQuest>) {
        try {
            val jsonArray = JSONArray()
            quests.forEach { quest ->
                val jsonObj = JSONObject().apply {
                    put("id", quest.id)
                    put("title", quest.title)
                    put("description", quest.description)
                    put("icon", quest.icon)
                    put("progress", quest.progress)
                    put("target", quest.target)
                    put("completed", quest.completed)
                }
                jsonArray.put(jsonObj)
            }
            // edit{} führt den Schreibvorgang atomar und asynchron aus,
            // sodass parallele Lese-/Schreibzugriffe konsistent bleiben.
            context.dataStore.edit { preferences ->
                preferences[DailyQuestPreferences.QUESTS_JSON] = jsonArray.toString()
            }
        } catch (e: Exception) {
            Log.e("DAILYQUEST_DS", "Fehler beim Speichern der Quests", e)
        }
    }

    /**
     * Liest den zuletzt gespeicherten JSON-String aus dem DataStore und
     * deserialisiert ihn zurück in eine Liste von [DailyQuest]-Objekten.
     *
     * .first() wird auf den [kotlinx.coroutines.flow.Flow] angewendet,
     * um einmalig den aktuellen Wert abzurufen, statt dauerhaft auf
     * Änderungen zu lauschen – einmaliger Ladevorgang beim
     * Start des [DailyQuestScreen].
     *
     * @return Die gespeicherte Liste an Daily Quests, oder eine leere
     * Liste, falls noch keine Daten vorhanden sind oder das Parsen
     * fehlschlägt (z. B. bei korrupten oder veralteten JSON-Daten).
     */
    suspend fun loadQuests(): List<DailyQuest> {
        val jsonString = context.dataStore.data
            .map { it[DailyQuestPreferences.QUESTS_JSON] }
            .first() ?: return emptyList()

        return try {
            val jsonArray = JSONArray(jsonString)
            val quests = mutableListOf<DailyQuest>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                quests.add(
                    DailyQuest(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        icon = obj.getString("icon"),
                        progress = obj.getInt("progress"),
                        target = obj.getInt("target"),
                        completed = obj.getBoolean("completed")
                    )
                )
            }
            quests
        } catch (e: Exception) {
            Log.e("DAILYQUEST_DS", "Fehler beim Laden der Quests", e)
            emptyList()
        }
    }

    /**
     * Speichert das Datum des letzten Daily-Quest-Resets, damit beim
     * nächsten App-Start geprüft werden kann, ob ein neuer Tag begonnen
     * hat und die Quests neu generiert werden müssen.
     *
     * @param date Datum im vom Aufrufer definierten Format (z. B. ISO-8601),
     * wird als reiner String ohne weitere Validierung gespeichert.
     */
    suspend fun saveLastResetDate(date: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[DailyQuestPreferences.LAST_RESET_DATE] = date
            }
        } catch (e: Exception) {
            Log.e("DAILYQUEST_DS", "Fehler beim Speichern des Reset-Datums", e)
        }
    }

    /**
     * Liest das zuletzt gespeicherte Reset-Datum aus dem DataStore.
     *
     * @return Das gespeicherte Datum als String, oder `null`, falls noch
     * kein Reset stattgefunden hat oder beim Lesen ein Fehler auftrat.
     */
    suspend fun loadLastResetDate(): String? {
        return try {
            context.dataStore.data
                .map { it[DailyQuestPreferences.LAST_RESET_DATE] }
                .first()
        } catch (e: Exception) {
            Log.e("DAILYQUEST_DS", "Fehler beim Laden des Reset-Datums", e)
            null
        }
    }
}