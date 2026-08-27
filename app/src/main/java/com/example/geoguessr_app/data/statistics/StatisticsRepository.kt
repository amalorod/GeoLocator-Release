package com.example.geoguessr_app.data.statistics

import android.util.Log
import com.example.geoguessr_app.data.profile.ProfileRepository
import com.example.geoguessr_app.domain.model.statistics.LifetimeStatistics
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

    suspend fun saveMatch(match: MatchStatistic) {
        val uid = ProfileRepository.profile.value?.playerId ?: auth.currentUser?.uid ?: return
        
        try {
            // 1. In Firebase speichern
            database.reference
                .child("users")
                .child(uid)
                .child("statistics")
                .child("matches")
                .push()
                .setValue(match)
                .await()

            // Update recent matches locally
            _recentMatches.value = (listOf(match) + _recentMatches.value).take(20)

            // 2. Lokale Statistik aktualisieren (in einer echten App würde man dies eher berechnen oder beobachten)
            val current = _statistics.value
            val updated = current.copy(
                gamesPlayed = current.gamesPlayed + 1,
                roundsPlayed = current.roundsPlayed + match.rounds,
                totalScore = current.totalScore + match.score,
                bestGameScore = maxOf(current.bestGameScore, match.score)
            )
            _statistics.value = updated
            
            // 3. Lifetime Stats in Firebase synchronisieren
            database.reference
                .child("users")
                .child(uid)
                .child("statistics")
                .child("lifetime")
                .setValue(updated)
                .await()

        } catch (e: Exception) {
            Log.e("STATISTICS", "Fehler beim Speichern der Statistik", e)
        }
    }

    suspend fun loadStatistics(targetUid: String? = null) {
        val uid = targetUid ?: auth.currentUser?.uid ?: return
        try {
            // Load Lifetime
            val snapshot = database.reference
                .child("users")
                .child(uid)
                .child("statistics")
                .child("lifetime")
                .get()
                .await()
            
            val stats = snapshot.getValue(LifetimeStatistics::class.java)
            if (stats != null) {
                _statistics.value = stats
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

            val matches = matchesSnapshot.children.mapNotNull { 
                it.getValue(MatchStatistic::class.java) 
            }.reversed()
            _recentMatches.value = matches

        } catch (e: Exception) {
            Log.e("STATISTICS", "Fehler beim Laden der Statistik", e)
        }
    }
}
