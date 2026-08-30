package com.example.geoguessr_app.data.dailyquest

import android.util.Log
import com.example.geoguessr_app.data.profile.ProfileRepository
import com.example.geoguessr_app.domain.model.dailyquest.DailyQuest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object DailyQuestRepository {

    private const val DB_URL = "https://bsi-geoguessr-app-63b7f-default-rtdb.europe-west1.firebasedatabase.app/"
    private val database = FirebaseDatabase.getInstance(DB_URL)
    private val auth = FirebaseAuth.getInstance()

    private val _quests = MutableStateFlow<List<DailyQuest>>(emptyList())
    val quests: StateFlow<List<DailyQuest>> = _quests.asStateFlow()

    private val DEFAULT_QUESTS = listOf(
        DailyQuest(
            id = "europe_explorer",
            title = "Europa Experte",
            description = "5 Orte in Europa unter 100 km",
            icon = "🌍",
            target = 5
        ),
        DailyQuest(
            id = "perfect_guess",
            title = "Präzision",
            description = "1 Ort unter 25 km erraten",
            icon = "🎯",
            target = 1
        ),
        DailyQuest(
            id = "multiplayer_win",
            title = "Teamplayer",
            description = "1 Multiplayer-Match gewinnen",
            icon = "🏆",
            target = 1
        ),
        DailyQuest(
            id = "battle_royale_win",
            title = "Überlebender",
            description = "1 Battle Royale gewinnen",
            icon = "❤️",
            target = 1
        ),
        DailyQuest(
            id = "pro_mode_guess",
            title = "Profi-Auge",
            description = "3 Orte im Pro-Modus erraten",
            icon = "👁️",
            target = 3
        )
    )

    suspend fun loadQuests() {
        val currentProfile = ProfileRepository.profile.value
        val isGuest = currentProfile == null
        
        if (isGuest) {
            resetQuests()
            return
        }

        try {
            val uid = currentProfile.playerId
            val snapshot = database.reference
                .child("users")
                .child(uid)
                .child("quests")
                .get()
                .await()
            
            if (snapshot.exists()) {
                val loadedQuests = snapshot.children.mapNotNull { it.getValue(DailyQuest::class.java) }
                _quests.value = loadedQuests
            } else {
                _quests.value = DEFAULT_QUESTS
                saveQuests(DEFAULT_QUESTS)
            }
        } catch (e: Exception) {
            Log.e("QUESTS", "Fehler beim Laden", e)
            // Fallback auf Default, damit die UI nicht leer bleibt/abstürzt
            if (_quests.value.isEmpty()) {
                _quests.value = DEFAULT_QUESTS
            }
        }
    }

    suspend fun saveQuests(quests: List<DailyQuest>) {
        // Lokales Update immer
        _quests.value = quests

        val currentProfile = ProfileRepository.profile.value
        val isGuest = currentProfile == null

        // Nur Cloud-Sync wenn kein Gast
        if (!isGuest && currentProfile != null) {
            val uid = currentProfile.playerId.ifEmpty { auth.currentUser?.uid } ?: return
            try {
                database.reference
                    .child("users")
                    .child(uid)
                    .child("quests")
                    .setValue(quests)
                    .await()
            } catch (e: Exception) {
                Log.e("QUESTS", "Fehler beim Speichern", e)
            }
        }
    }

    suspend fun updateQuestProgress(questId: String, increment: Int = 1): DailyQuest? {
        val currentQuests = _quests.value.toMutableList()
        val index = currentQuests.indexOfFirst { it.id == questId }
        if (index != -1) {
            val quest = currentQuests[index]
            if (quest.completed) return null
            
            val newProgress = quest.progress + increment
            val isCompleted = newProgress >= quest.target
            val updatedQuest = quest.copy(
                progress = newProgress,
                completed = isCompleted
            )
            currentQuests[index] = updatedQuest
            saveQuests(currentQuests)
            
            return if (isCompleted) updatedQuest else null
        }
        return null
    }

    fun resetQuests() {
        _quests.value = DEFAULT_QUESTS
    }
}
