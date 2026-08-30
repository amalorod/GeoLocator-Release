package com.example.geoguessr_app.data.datastore

import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object StatisticsPreferences {

    val GAMES_PLAYED =
        intPreferencesKey("games_played")

    val ROUNDS_PLAYED =
        intPreferencesKey("rounds_played")

    val TOTAL_SCORE =
        intPreferencesKey("total_score")

    val BEST_GAME_SCORE =
        intPreferencesKey("best_game_score")

    val TOTAL_DISTANCE =
        doublePreferencesKey("total_distance")

    val RECENT_MATCHES =
        androidx.datastore.preferences.core.stringPreferencesKey("recent_matches")
}