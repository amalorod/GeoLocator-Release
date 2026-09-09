package alic.malorodow.geoguessr_app.data.dailyquest

import android.util.Log
import alic.malorodow.geoguessr_app.data.datastore.DailyQuestDataStoreRepository
import alic.malorodow.geoguessr_app.data.firebase.FirebaseAuthRepository
import alic.malorodow.geoguessr_app.data.profile.ProfileRepository
import alic.malorodow.geoguessr_app.domain.model.dailyquest.DailyQuest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verwaltet die täglichen Herausforderungen (Daily Quests) des Nutzers.
 *
 * HINWEIS: Injiziert ProfileRepository, um den aktuellen
 * Login-Zustand zu bestimmen (siehe isGuest in loadQuests()/
 * saveQuests()). Die Abhängigkeitsrichtung verläuft ausschließlich
 * einseitig hierhin – ProfileRepository selbst besitzt umgekehrt
 * keine Abhängigkeit zu dieser Klasse, wodurch keine zirkuläre
 * Abhängigkeit entsteht.
 *
 * Datenspeicherung: Gast-Nutzer persistieren ihren Fortschritt lokal
 * über [DailyQuestDataStoreRepository] inklusive automatischem Tages-Reset.
 *
 * HINWEIS: Im Gegensatz zu StatisticsRepository, das für
 * Gast- und Account-Fall zwei getrennte Methoden (loadLocalStatistics()/
 * loadStatistics(uid)) anbietet, übernimmt hier die einzige Methode
 * loadQuests() beide Fälle selbst, da sie profileRepository.profile.value
 * intern abfragt. Aufrufer müssen daher nach jedem Profilwechsel lediglich
 * loadQuests() erneut aufrufen, ohne selbst zwischen Gast und Account
 * unterscheiden zu müssen.
 */
@Singleton
class DailyQuestRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val authRepository: FirebaseAuthRepository,
    private val profileRepository: ProfileRepository,
    private val dataStoreRepository: DailyQuestDataStoreRepository
) {
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

    // Bestimmt das heutige Datum
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /**
     * Lädt die Quests des aktuell angemeldeten Nutzers aus Firebase (oder lokal für Gäste)
     * und prüft dabei automatisch, ob ein neuer Tag (nach 00:00 Uhr) begonnen hat,
     * um die Quests täglich zurückzusetzen.
     */
    suspend fun loadQuests() {
        val currentProfile = profileRepository.profile.value
        val isGuest = currentProfile == null
        val today = getTodayDateString()

        if (isGuest) {
            val localQuests = dataStoreRepository.loadQuests()
            val lastReset = dataStoreRepository.loadLastResetDate()
            if (localQuests.isNotEmpty() && lastReset == today) {
                _quests.value = localQuests
            } else {
                val freshQuests = DEFAULT_QUESTS.map { it.copy(progress = 0, completed = false) }
                saveQuests(freshQuests)
                dataStoreRepository.saveLastResetDate(today)
            }
            return
        }

        try {
            val uid = currentProfile.playerId
            val userRef = database.reference.child("users").child(uid)
            val questsSnapshot = userRef.child("quests").get().await()
            val lastResetSnapshot = userRef.child("lastResetDate").get().await()
            val lastResetDate = lastResetSnapshot.getValue(String::class.java)

            if (questsSnapshot.exists() && lastResetDate == today) {
                val loadedQuests =
                    questsSnapshot.children.mapNotNull { it.getValue(DailyQuest::class.java) }
                _quests.value = loadedQuests
            } else {
                val freshQuests = DEFAULT_QUESTS.map { it.copy(progress = 0, completed = false) }
                _quests.value = freshQuests
                userRef.child("quests").setValue(freshQuests).await()
                userRef.child("lastResetDate").setValue(today).await()
            }
        } catch (e: Exception) {
            Log.e("QUESTS", "Fehler beim Laden", e)
            if (_quests.value.isEmpty()) {
                _quests.value = DEFAULT_QUESTS.map { it.copy(progress = 0, completed = false) }
            }
        }
    }

    /**
     * Aktualisiert den Quest-Zustand und synchronisiert ihn mit Firebase bzw. DataStore
     * inklusive des heutigen Datums.
     */
    suspend fun saveQuests(quests: List<DailyQuest>) {
        _quests.value = quests
        val today = getTodayDateString()

        val currentProfile = profileRepository.profile.value
        val isGuest = currentProfile == null

        if (isGuest) {
            dataStoreRepository.saveQuests(quests)
            dataStoreRepository.saveLastResetDate(today)
            return
        }

        if (currentProfile != null) {
            val uid = currentProfile.playerId.ifEmpty { authRepository.currentUid() } ?: return
            try {
                val userRef = database.reference.child("users").child(uid)
                userRef.child("quests").setValue(quests).await()
                userRef.child("lastResetDate").setValue(today).await()
            } catch (e: Exception) {
                Log.e("QUESTS", "Fehler beim Speichern", e)
            }
        }
    }

    /**
     * Erhöht den Fortschritt einer einzelnen Quest um den angegebenen
     * Wert und markiert sie bei Erreichen des Ziels als abgeschlossen.
     */
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

    /**
     * Setzt die Quests vollständig zurück und aktualisiert das Reset-Datum.
     */
    suspend fun clearLocalQuests() {
        val freshQuests = DEFAULT_QUESTS.map { it.copy(progress = 0, completed = false) }
        _quests.value = freshQuests
        dataStoreRepository.saveQuests(freshQuests)
        dataStoreRepository.saveLastResetDate(getTodayDateString())
    }
}
