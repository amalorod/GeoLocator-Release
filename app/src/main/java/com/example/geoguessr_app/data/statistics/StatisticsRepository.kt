package com.example.geoguessr_app.data.statistics

import android.util.Log
import com.example.geoguessr_app.data.profile.ProfileRepository
import com.example.geoguessr_app.domain.statistics.LifetimeStatistics
import com.example.geoguessr_app.domain.model.statistics.MatchStatistic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object StatisticsRepository {

    private const val DB_URL = "https://bsi-geoguessr-app-63b7f-default-rtdb.europe-west1.firebasedatabase.app/"
    private val database = FirebaseDatabase.getInstance(DB_URL)
    private val auth = FirebaseAuth.getInstance()

    private val _statistics = MutableStateFlow(LifetimeStatistics())
    val statistics: StateFlow<LifetimeStatistics> = _statistics.asStateFlow()

    private val _recentMatches = MutableStateFlow<List<MatchStatistic>>(emptyList())
    val recentMatches: StateFlow<List<MatchStatistic>> = _recentMatches.asStateFlow()

    private val _topPlayers = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val topPlayers: StateFlow<List<LeaderboardEntry>> = _topPlayers.asStateFlow()

    suspend fun saveMatch(match: MatchStatistic) {
        val currentProfile = ProfileRepository.profile.value
        val isGuest = currentProfile == null || currentProfile.playerName == "Spieler"
        
        // Lokale Statistik IMMER aktualisieren
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

        // Nur in Firebase speichern, wenn wir ein echtes Profil haben
        if (!isGuest && currentProfile != null) {
            val uid = currentProfile.playerId.ifEmpty { auth.currentUser?.uid } ?: return
            try {
                // 1. Match in Firebase speichern
                database.reference
                    .child("users")
                    .child(uid)
                    .child("statistics")
                    .child("matches")
                    .push()
                    .setValue(match)
                    .await()

                // 2. Lifetime Stats in Firebase synchronisieren
                database.reference
                    .child("users")
                    .child(uid)
                    .child("statistics")
                    .child("lifetime")
                    .setValue(updated)
                    .await()
                
                Log.d("STATISTICS", "Statistik für Nutzer ${currentProfile?.playerName} in Firebase gespeichert.")
            } catch (e: Exception) {
                Log.e("STATISTICS", "Fehler beim Cloud-Speichern", e)
            }
        } else {
            Log.d("STATISTICS", "Statistik nur lokal gespeichert (Gast-Modus).")
        }
    }

    suspend fun loadStatistics(targetUid: String? = null) {
        val uid = targetUid ?: auth.currentUser?.uid ?: run {
            // Falls gar keine UID (auch nicht anonym) vorhanden ist, stats zurücksetzen
            clearLocalStatistics()
            return
        }
        
        try {
            Log.d("STATISTICS", "Lade Statistiken für UID: $uid")
            // Load Lifetime
            val snapshot = database.reference
                .child("users")
                .child(uid)
                .child("statistics")
                .child("lifetime")
                .get()
                .await()
            
            if (snapshot.exists()) {
                _statistics.value = snapshot.getValue(LifetimeStatistics::class.java) ?: LifetimeStatistics()
            } else {
                _statistics.value = LifetimeStatistics()
            }

            // Load Matches
            val matchesSnapshot = database.reference
                .child("users")
                .child(uid)
                .child("statistics")
                .child("matches")
                .limitToLast(20)
                .get()
                .await()

            if (matchesSnapshot.exists()) {
                val matches = matchesSnapshot.children.mapNotNull { 
                    it.getValue(MatchStatistic::class.java) 
                }.reversed()
                _recentMatches.value = matches
            } else {
                _recentMatches.value = emptyList()
            }

            // Load Leaderboard
            loadLeaderboard()

        } catch (e: Exception) {
            Log.e("STATISTICS", "Fehler beim Laden der Statistik", e)
        }
    }

    fun clearLocalStatistics() {
        _statistics.value = LifetimeStatistics()
        _recentMatches.value = emptyList()
    }

    suspend fun loadLeaderboard() {
        try {
            val snapshot = database.reference.child("users").get().await()
            val entries = mutableListOf<LeaderboardEntry>()

            for (userSnapshot in snapshot.children) {
                val profile = userSnapshot.child("profile").getValue(com.example.geoguessr_app.domain.model.profile.PlayerProfile::class.java)
                val stats = userSnapshot.child("statistics").child("lifetime").getValue(LifetimeStatistics::class.java)
                
                if (profile != null && stats != null) {
                    entries.add(LeaderboardEntry(profile.playerName, stats))
                }
            }

            // Sortiere nach durchschnittlicher Punktzahl absteigend und nimm die Top 3
            _topPlayers.value = entries
                .sortedByDescending { 
                    if (it.stats.gamesPlayed > 0) it.stats.totalScore.toDouble() / it.stats.gamesPlayed 
                    else 0.0 
                }
                .take(3)
                
            Log.d("STATISTICS", "Leaderboard geladen: ${_topPlayers.value.size} Spieler")
        } catch (e: Exception) {
            Log.e("STATISTICS", "Fehler beim Laden des Leaderboards", e)
        }
    }
}

data class LeaderboardEntry(
    val playerName: String,
    val stats: LifetimeStatistics
)
