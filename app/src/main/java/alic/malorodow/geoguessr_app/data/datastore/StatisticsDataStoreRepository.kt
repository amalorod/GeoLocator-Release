package alic.malorodow.geoguessr_app.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import alic.malorodow.geoguessr_app.domain.model.statistics.MatchStatistic
import alic.malorodow.geoguessr_app.domain.statistics.LifetimeStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistiert die Lifetime-Statistiken sowie die zuletzt gespielten
 * Partien eines Gast-Nutzers lokal auf dem Gerät über Jetpack
 * DataStore (siehe Doku, Kapitel 2.5 „Datenhaltung“).
 *
 * Wird ausschließlich für Gast-Nutzer verwendet, die kein Firebase-
 * Profil besitzen. Für angemeldete Nutzer ist [StatisticsRepository]
 * über Firebase Realtime Database die alleinige (Single Source of Truth).
 * Eine Vermischung bzw. Addition beider Datenquellen wird vermieden, um
 * Inkonsistenzen zwischen lokalem und Cloud-Stand zu verhindern.
 *
 * Die Lifetime-Statistiken werden als einzelne Preference-Schlüssel
 * abgelegt [StatisticsPreferences], da DataStore mit dem
 * Preferences-DataStore-Ansatz keine verschachtelten Objekte direkt
 * unterstützt. Die Partienliste hingegen wird als einzelner
 * JSON-String gespeichert (siehe saveRecentMatches()), da eine Liste
 * variabler Länge sich nicht sinnvoll in einzelne Preference-Schlüssel
 * abbilden lässt.
 */
class StatisticsDataStoreRepository(
    private val context: Context
) {

    /**
     * Beobachtbarer Datenstrom der lokal gespeicherten
     * Lifetime-Statistiken. Fehlt ein Wert (z. B. beim allerersten
     * Start), wird der jeweilige Standardwert von [LifetimeStatistics]
     * verwendet.
     */
    val statistics: Flow<LifetimeStatistics> =
        context.dataStore.data.map { preferences ->
            LifetimeStatistics(
                gamesPlayed = preferences[StatisticsPreferences.GAMES_PLAYED] ?: 0,
                roundsPlayed = preferences[StatisticsPreferences.ROUNDS_PLAYED] ?: 0,
                totalScore = preferences[StatisticsPreferences.TOTAL_SCORE] ?: 0,
                bestGameScore = preferences[StatisticsPreferences.BEST_GAME_SCORE] ?: 0,
                totalDistanceKm = preferences[StatisticsPreferences.TOTAL_DISTANCE] ?: 0.0
            )
        }

    /**
     * Schreibt sämtliche Felder der übergebenen Statistik in den
     * DataStore. Die Funktion edit() führt den Schreibvorgang transaktional aus,
     * sodass niemals nur teilweise aktualisierte Werte gelesen werden
     * können.
     *
     * ANMERKUNG: Wird von saveMatch() im [StatisticsRepository] stets
     * gemeinsam mit saveRecentMatches() aufgerufen, jedoch als zwei
     * getrennte edit()-Transaktionen statt einer gemeinsamen. Das ist
     * für die praktische Anwendung unkritisch, da DataStore Schreibzugriffe
     * ohnehin serialisiert. Theoretisch könnten aber beide Werte bei einem
     * Absturz exakt zwischen den beiden Aufrufen kurzfristig
     * inkonsistent zueinander sein.
     */
    suspend fun saveStatistics(statistics: LifetimeStatistics) {
        context.dataStore.edit { preferences ->
            preferences[StatisticsPreferences.GAMES_PLAYED] = statistics.gamesPlayed
            preferences[StatisticsPreferences.ROUNDS_PLAYED] = statistics.roundsPlayed
            preferences[StatisticsPreferences.TOTAL_SCORE] = statistics.totalScore
            preferences[StatisticsPreferences.BEST_GAME_SCORE] = statistics.bestGameScore
            preferences[StatisticsPreferences.TOTAL_DISTANCE] = statistics.totalDistanceKm
        }
    }

    /**
     * Speichert die Liste der letzten Partien als serialisierten
     * JSON-String, da DataStore Preferences keine Listen komplexer
     * Objekte nativ unterstützt.
     *
     * Auf eine vollwertige Serialisierungsbibliothek (z. B.
     * kotlinx.serialization) wird hier bewusst verzichtet und
     * stattdessen manuell mit org.json gearbeitet, um für dieses
     * einzelne Feld keine zusätzliche Abhängigkeit einzuführen.
     */
    suspend fun saveRecentMatches(matches: List<MatchStatistic>) = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            matches.forEach { match ->
                val jsonObj = JSONObject().apply {
                    put("timestamp", match.timestamp)
                    put("gameMode", match.gameMode)
                    put("score", match.score)
                    put("rounds", match.rounds)
                    put("distanceKm", match.distanceKm)
                    put("won", match.won)
                    put("multiplayer", match.multiplayer)
                }
                jsonArray.put(jsonObj)
            }
            context.dataStore.edit { preferences ->
                preferences[StatisticsPreferences.RECENT_MATCHES] = jsonArray.toString()
            }
        } catch (e: Exception) {
            Log.e("DATASTORE", "Fehler beim Speichern der Matches", e)
        }
    }

    /**
     * Lädt die Liste der letzten Partien aus dem gespeicherten
     * JSON-String zurück. Liefert eine leere Liste, falls noch kein
     * Wert existiert (z. B. beim allerersten Start) oder das Parsen
     * fehlschlägt, statt eine Exception nach außen dringen zu lassen.
     */
    suspend fun loadRecentMatches(): List<MatchStatistic> = withContext(Dispatchers.IO) {
        val jsonString = context.dataStore.data
            .map { it[StatisticsPreferences.RECENT_MATCHES] }
            .first() ?: return@withContext emptyList()

        try {
            val jsonArray = JSONArray(jsonString)
            val matches = mutableListOf<MatchStatistic>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                matches.add(
                    MatchStatistic(
                        timestamp = obj.getLong("timestamp"),
                        gameMode = obj.getString("gameMode"),
                        score = obj.getInt("score"),
                        rounds = obj.getInt("rounds"),
                        distanceKm = obj.getDouble("distanceKm"),
                        won = obj.getBoolean("won"),
                        multiplayer = obj.getBoolean("multiplayer")
                    )
                )
            }
            matches
        } catch (e: Exception) {
            Log.e("DATASTORE", "Fehler beim Laden der Matches", e)
            emptyList()
        }
    }
}