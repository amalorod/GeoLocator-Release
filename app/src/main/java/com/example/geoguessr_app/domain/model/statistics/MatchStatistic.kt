package com.example.geoguessr_app.domain.model.statistics

data class MatchStatistic(
    val timestamp: Long = 0,
    val gameMode: String = "",
    val score: Int = 0,
    val rounds: Int = 0,
    val won: Boolean = false,
    val multiplayer: Boolean = false
)
