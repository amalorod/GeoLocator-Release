package com.example.geoguessr_app.data.dailyquest

import android.util.Log
import com.example.geoguessr_app.data.datastore.DailyQuestDataStoreRepository
import com.example.geoguessr_app.data.profile.ProfileRepository
import com.example.geoguessr_app.domain.model.dailyquest.DailyQuest
import com.example.geoguessr_app.data.firebase.FirebaseAuthRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verwaltet die täglichen Herausforderungen (Daily Quests) des Nutzers.
 *
 * ARCHITEKTUR-HINWEIS: Injiziert ProfileRepository, um den aktuellen
 * Login-Zustand zu bestimmen (siehe isGuest in loadQuests()/
 * saveQuests()). Die Abhängigkeitsrichtung verläuft ausschließlich
 * einseitig hierhin – ProfileRepository selbst besitzt umgekehrt
 * keine Abhängigkeit zu dieser Klasse, wodurch keine zirkuläre
 * Abhängigkeit entsteht.
 *
 * GAST-PERSISTENZ: Im Gegensatz zur ursprünglichen Implementierung
 * (siehe Doku-Historie) wird der Quest-Fortschritt von Gast-Nutzern
 * jetzt über DailyQuestDataStoreRepository persistiert und überlebt
 * damit einen App-Neustart, analog zu StatisticsRepository.
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

    /**
     * Feste Liste der aktuell verfügbaren Tagesherausforderungen.
     *
     * ARCHITEKTUR-HINWEIS: Trotz des Namens "Daily" wird die Auswahl
     * hier statisch im Code hinterlegt, statt sich täglich zufällig
     * oder serverseitig zu ändern. Ein neuer Nutzer bzw. ein neuer
     * Firebase-Datensatz erhält daher stets exakt dieselben fünf
     * Quests (siehe loadQuests(), Zweig "snapshot existiert nicht").
     * Eine echte Rotation wäre ein guter Kandidat für den
     * Erweiterungshorizont.
     */
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

    /**
     * Lädt die Quests des aktuell angemeldeten Nutzers aus Firebase,
     * oder setzt für Gäste die Standard-Quests zurück.
     *
     * Existiert für einen angemeldeten Nutzer noch kein Quest-Datensatz
     * (z. B. erster Login), werden die DEFAULT_QUESTS einmalig sowohl
     * lokal gesetzt als auch in Firebase persistiert, damit der Nutzer
     * ab diesem Zeitpunkt einen eigenen, unabhängig fortschreibbaren
     * Datensatz besitzt.
     */
    suspend fun loadQuests() {
        val currentProfile = profileRepository.profile.value
        val isGuest = currentProfile == null

        if (isGuest) {
            val localQuests = dataStoreRepository.loadQuests()
            if (localQuests.isNotEmpty()) {
                _quests.value = localQuests
            } else {
                resetQuests()
            }
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
                val loadedQuests =
                    snapshot.children.mapNotNull { it.getValue(DailyQuest::class.java) }
                _quests.value = loadedQuests
            } else {
                _quests.value = DEFAULT_QUESTS
                saveQuests(DEFAULT_QUESTS)
            }
        } catch (e: Exception) {
            Log.e("QUESTS", "Fehler beim Laden", e)
            if (_quests.value.isEmpty()) {
                _quests.value = DEFAULT_QUESTS
            }
        }
    }

    /**
     * Aktualisiert den lokalen Quest-Zustand und synchronisiert ihn
     * bei angemeldeten Nutzern zusätzlich mit Firebase. Für Gäste
     * bleibt die Aktualisierung ausschließlich im Speicher (siehe
     * Klassendokumentation zur fehlenden Gast-Persistenz).
     */
    suspend fun saveQuests(quests: List<DailyQuest>) {
        _quests.value = quests

        val currentProfile = profileRepository.profile.value
        val isGuest = currentProfile == null

        if (isGuest) {
            dataStoreRepository.saveQuests(quests)
            return
        }

        if (currentProfile != null) {
            val uid = currentProfile.playerId.ifEmpty { authRepository.currentUid() } ?: return
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

    /**
     * Erhöht den Fortschritt einer einzelnen Quest um den angegebenen
     * Wert und markiert sie bei Erreichen des Ziels als abgeschlossen.
     *
     * Bereits abgeschlossene Quests werden ignoriert (early return
     * über null), um ein versehentliches Überschreiten des Ziels oder
     * eine erneute "Abschluss"-Benachrichtigung zu verhindern.
     *
     * @return die aktualisierte Quest, falls sie durch diesen Aufruf
     *   neu abgeschlossen wurde (z. B. für eine Erfolgs-Anzeige im
     *   UI), sonst null.
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
     * Setzt den Quest-Zustand auf die Standard-Quests zurück, ohne
     * Firebase zu kontaktieren. Wird für den Gast-Modus sowie beim
     * Übergang zwischen Login-Zuständen verwendet (siehe
     * ProfileRepository).
     */
    fun resetQuests() {
        _quests.value = DEFAULT_QUESTS
    }

    /**
     * Setzt die Quests vollständig zurück – sowohl den In-Memory-Zustand
     * als auch die lokal persistierten Gast-Daten. Wird beim Übergang
     * von Gast zu angemeldetem Nutzer aufgerufen, um sicherzustellen,
     * dass keine alten Gast-Quests nach einem Login weiterbestehen
     * ("Verwerfen-beim-Login"-Strategie).
     */
    suspend fun clearLocalQuests() {
        _quests.value = DEFAULT_QUESTS
        dataStoreRepository.saveQuests(DEFAULT_QUESTS)
    }
}
