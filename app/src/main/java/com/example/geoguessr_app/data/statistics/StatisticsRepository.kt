package com.example.geoguessr_app.data.statistics

import android.content.Context
import android.util.Log
import com.example.geoguessr_app.data.datastore.StatisticsDataStoreRepository
import com.example.geoguessr_app.data.profile.ProfileRepository
import com.example.geoguessr_app.domain.model.statistics.MatchStatistic
import com.example.geoguessr_app.domain.statistics.LifetimeStatistics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

/**
 * Verwaltet sämtliche spielübergreifenden Statistiken sowie das
 * globale Leaderboard (siehe Doku, Kapitel 4.4 „Statistics Screen“ und
 * 5.6 „Profil-, Statistik- und Cloud-System“).
 *
 * ARCHITEKTUR-HINWEIS: Als object-Singleton implementiert und mit einer
 * fest im Code hinterlegten Firebase-URL sowie eigener FirebaseAuth-
 * Instanz versehen, statt über Hilt injiziert zu werden. Dieses
 * Repository greift außerdem direkt auf das ebenfalls als Singleton
 * implementierte [ProfileRepository] zu. Für eine konsequente
 * Dependency-Injection-Architektur (siehe Doku, Kapitel 2.4) sollte
 * dies künftig über Konstruktor-Injection erfolgen.
 *
 * Kombiniert zwei strikt getrennte Datenquellen: Firebase Realtime
 * Database für angemeldete Nutzer und lokalen Jetpack DataStore für
 * Gast-Nutzer (siehe saveMatch). Welche Quelle gilt, wird ausschließlich
 * anhand des Login-Zustands in [ProfileRepository] entschieden – nicht
 * anhand von Heuristiken über Feldwerte, siehe Anmerkung bei isGuest.
 */
object StatisticsRepository {

    private const val DB_URL =
        "https://bsi-geoguessr-app-63b7f-default-rtdb.europe-west1.firebasedatabase.app/"
    private val database = FirebaseDatabase.getInstance(DB_URL)
    private val auth = FirebaseAuth.getInstance()

    private val _statistics = MutableStateFlow(LifetimeStatistics())
    val statistics: StateFlow<LifetimeStatistics> = _statistics.asStateFlow()

    private val _recentMatches = MutableStateFlow<List<MatchStatistic>>(emptyList())
    val recentMatches: StateFlow<List<MatchStatistic>> = _recentMatches.asStateFlow()

    private val _topPlayers = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val topPlayers: StateFlow<List<LeaderboardEntry>> = _topPlayers.asStateFlow()

    // Wird über initialize() befüllt (siehe MainActivity), analog zum
    // bestehenden Muster in ProfileRepository. ARCHITEKTUR-HINWEIS:
    // Dieselbe Kritik wie bei ProfileRepository gilt hier – langfristig
    // sollte dies über Hilt injiziert werden statt über ein manuelles
    // initialize().
    private var dataStoreRepository: StatisticsDataStoreRepository? = null

    fun initialize(context: Context) {
        dataStoreRepository = StatisticsDataStoreRepository(context.applicationContext)
    }

    /**
     * Speichert das Ergebnis einer abgeschlossenen Partie.
     *
     * Striktes Trennungsprinzip zur Vermeidung von Dateninkonsistenzen:
     * Gast-Statistiken werden ausschließlich lokal über DataStore
     * persistiert, Statistiken angemeldeter Nutzer ausschließlich in
     * Firebase.
     */
    suspend fun saveMatch(match: MatchStatistic) {
        val currentProfile = ProfileRepository.profile.value

        // Der Login-Zustand wird ausschließlich über das Vorhandensein
        // eines Profils entschieden. ProfileRepository setzt profile
        // entweder auf ein vollständig geladenes Firebase-Profil oder
        // explizit auf null (siehe loadProfile), sodass hier keine
        // fehleranfällige Heuristik über Feldwerte mehr nötig ist.
        val isGuest = currentProfile == null

        val current = _statistics.value
        val updated = current.copy(
            gamesPlayed = current.gamesPlayed + 1,
            roundsPlayed = current.roundsPlayed + match.rounds,
            totalScore = current.totalScore + match.score,
            bestGameScore = maxOf(current.bestGameScore, match.score),
            totalDistanceKm = current.totalDistanceKm + match.distanceKm
        )
        _statistics.value = updated
        _recentMatches.value = (listOf(match) + _recentMatches.value).take(20)

        if (isGuest) {
            // Gast-Pfad: Persistiert ausschließlich lokal auf dem
            // Gerät. Firebase wird hier bewusst nicht kontaktiert, um
            // eine spätere Vermischung mit Cloud-Daten auszuschließen.
            dataStoreRepository?.saveStatistics(updated)
            dataStoreRepository?.saveRecentMatches(_recentMatches.value)
            Log.d("STATISTICS", "Statistik und Matches lokal gespeichert (Gast-Modus).")
            return
        }

        // Eingeloggter Pfad: Persistiert ausschließlich in Firebase.
        val uid = currentProfile?.playerId?.ifEmpty { auth.currentUser?.uid } ?: return
        try {
            database.reference
                .child("users")
                .child(uid)
                .child("statistics")
                .child("matches")
                .push()
                .setValue(match)
                .await()

            database.reference
                .child("users")
                .child(uid)
                .child("statistics")
                .child("lifetime")
                .setValue(updated)
                .await()

            Log.d(
                "STATISTICS",
                "Statistik für Nutzer ${currentProfile.playerName} in Firebase gespeichert."
            )
        } catch (e: Exception) {
            Log.e("STATISTICS", "Fehler beim Cloud-Speichern", e)
        }
    }

    /**
     * Lädt die Lifetime-Statistiken sowie die letzten Partien eines
     * Nutzers aus Firebase.
     *
     * @param targetUid optionale UID eines anderen Nutzers, dessen
     *   Statistiken geladen werden sollen. Ohne Angabe wird die UID
     *   des aktuell angemeldeten Nutzers verwendet.
     */
    suspend fun loadStatistics(targetUid: String? = null) {
        val uid = targetUid ?: auth.currentUser?.uid ?: return
        try {
            val statsSnapshot = database.reference
                .child("users")
                .child(uid)
                .child("statistics")
                .child("lifetime")
                .get()
                .await()

            _statistics.value = if (statsSnapshot.exists()) {
                statsSnapshot.getValue(LifetimeStatistics::class.java) ?: LifetimeStatistics()
            } else {
                LifetimeStatistics()
            }

            val matchesSnapshot = database.reference
                .child("users")
                .child(uid)
                .child("statistics")
                .child("matches")
                .get()
                .await()

            val matches = mutableListOf<MatchStatistic>()
            if (matchesSnapshot.exists()) {
                for (matchSnap in matchesSnapshot.children) {
                    matchSnap.getValue(MatchStatistic::class.java)?.let { matches.add(it) }
                }
            }
            // Sortierung nach Zeitstempel absteigend (neueste zuerst),
            // da Firebase keine garantierte Sortierreihenfolge liefert.
            _recentMatches.value = matches.sortedByDescending { it.timestamp }.take(20)

            // Leaderboard wird mit aktualisiert, damit nach dem Laden
            // eines Profils stets ein aktueller Rangvergleich verfügbar
            // ist (siehe Doku, Kapitel 5.6).
            loadLeaderboard()

            Log.d("STATISTICS", "Statistiken für Nutzer $uid aus Firebase geladen.")
        } catch (e: Exception) {
            Log.e("STATISTICS", "Fehler beim Laden der Statistiken aus Firebase", e)
        }
    }

    /**
     * Lädt die persistierten Lifetime-Statistiken sowie die zuletzt
     * gespielten Partien eines Gast-Nutzers aus dem lokalen DataStore.
     * Wird beim App-Start im Gast-Fall sowie nach einem Logout
     * aufgerufen (siehe ProfileRepository.loadProfile() und logout()),
     * damit Gast-Fortschritt einen App-Neustart überlebt.
     */
    suspend fun loadLocalStatistics() {
        val repository = dataStoreRepository ?: return
        try {
            _statistics.value = repository.statistics.first()
            val localMatches = repository.loadRecentMatches()
            _recentMatches.value = localMatches

            Log.d(
                "STATISTICS",
                "Lokale Statistiken und ${localMatches.size} Matches erfolgreich geladen."
            )
        } catch (e: Exception) {
            Log.e("STATISTICS", "Fehler beim Laden lokaler Statistiken", e)
        }
    }

    /**
     * Setzt die Statistiken vollständig zurück – sowohl den
     * In-Memory-Zustand als auch die lokal persistierten Gast-Daten.
     * Wird beim Übergang von Gast zu angemeldetem Nutzer aufgerufen
     * (siehe ProfileRepository.loginWithUsername und createAndLogin),
     * um sicherzustellen, dass keine alten Gast-Statistiken nach einem
     * Login weiterbestehen ("Verwerfen-beim-Login"-Strategie).
     */
    suspend fun clearLocalStatistics() {
        _statistics.value = LifetimeStatistics()
        _recentMatches.value = emptyList()
        dataStoreRepository?.saveStatistics(LifetimeStatistics())
        dataStoreRepository?.saveRecentMatches(emptyList())
    }

    /**
     * Lädt und berechnet das globale Leaderboard der Top 3 Spieler.
     *
     * ARCHITEKTUR-HINWEIS: Lädt sämtliche Nutzerdaten unter "users"
     * vollständig und filtert/sortiert clientseitig. Bei wachsender
     * Nutzerzahl wird dieser Ansatz zunehmend ineffizient; für einen
     * produktiven Einsatz wäre eine serverseitige, indizierte
     * Sortierung sinnvoller.
     */
    suspend fun loadLeaderboard() {
        try {
            val snapshot = database.reference.child("users").get().await()
            val entries = mutableListOf<LeaderboardEntry>()

            for (userSnapshot in snapshot.children) {
                val profile = userSnapshot.child("profile")
                    .getValue(com.example.geoguessr_app.domain.model.profile.PlayerProfile::class.java)
                val stats = userSnapshot.child("statistics").child("lifetime")
                    .getValue(LifetimeStatistics::class.java)

                if (profile != null && stats != null) {
                    entries.add(LeaderboardEntry(profile.playerName, stats))
                }
            }

            _topPlayers.value = entries
                .sortedByDescending {
                    if (it.stats.gamesPlayed > 0) {
                        it.stats.totalScore.toDouble() / it.stats.gamesPlayed
                    } else {
                        0.0
                    }
                }
                .take(3)

            Log.d("STATISTICS", "Leaderboard geladen: ${_topPlayers.value.size} Spieler")
        } catch (e: Exception) {
            Log.e("STATISTICS", "Fehler beim Laden des Leaderboards", e)
        }
    }
}

/**
 * Repräsentiert einen einzelnen Eintrag im globalen Leaderboard.
 *
 * @property playerName Anzeigename des Spielers.
 * @property stats Lifetime-Statistiken, aus denen der
 *   Leaderboard-Rang berechnet wird (siehe [loadLeaderboard]).
 */
data class LeaderboardEntry(
    val playerName: String,
    val stats: LifetimeStatistics
)