package com.example.geoguessr_app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.geoguessr_app.domain.statistics.LifetimeStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StatisticsDataStoreRepository(
    private val context: Context
) {

    val statistics: Flow<LifetimeStatistics> =
        context.dataStore.data.map { preferences ->

            LifetimeStatistics(
                gamesPlayed =
                    preferences[
                        StatisticsPreferences.GAMES_PLAYED
                    ] ?: 0,

                roundsPlayed =
                    preferences[
                        StatisticsPreferences.ROUNDS_PLAYED
                    ] ?: 0,

                totalScore =
                    preferences[
                        StatisticsPreferences.TOTAL_SCORE
                    ] ?: 0,

                bestGameScore =
                    preferences[
                        StatisticsPreferences.BEST_GAME_SCORE
                    ] ?: 0,

                totalDistanceKm =
                    preferences[
                        StatisticsPreferences.TOTAL_DISTANCE
                    ] ?: 0.0
            )
        }

    suspend fun saveStatistics(
        statistics: LifetimeStatistics
    ) {

        context.dataStore.edit { preferences ->

            preferences[
                StatisticsPreferences.GAMES_PLAYED
            ] = statistics.gamesPlayed

            preferences[
                StatisticsPreferences.ROUNDS_PLAYED
            ] = statistics.roundsPlayed

            preferences[
                StatisticsPreferences.TOTAL_SCORE
            ] = statistics.totalScore

            preferences[
                StatisticsPreferences.BEST_GAME_SCORE
            ] = statistics.bestGameScore

            preferences[
                StatisticsPreferences.TOTAL_DISTANCE
            ] = statistics.totalDistanceKm
        }
    }
}