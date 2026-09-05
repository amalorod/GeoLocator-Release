package com.example.geoguessr_app.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import com.example.geoguessr_app.domain.model.dailyquest.DailyQuest
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
 * über Firebase Realtime Database die alleinige Quelle der Wahrheit.
 */
class DailyQuestDataStoreRepository(
    private val context: Context
) {
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
            context.dataStore.edit { preferences ->
                preferences[DailyQuestPreferences.QUESTS_JSON] = jsonArray.toString()
            }
        } catch (e: Exception) {
            Log.e("DAILYQUEST_DS", "Fehler beim Speichern der Quests", e)
        }
    }

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

    suspend fun saveLastResetDate(date: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[DailyQuestPreferences.LAST_RESET_DATE] = date
            }
        } catch (e: Exception) {
            Log.e("DAILYQUEST_DS", "Fehler beim Speichern des Reset-Datums", e)
        }
    }

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
