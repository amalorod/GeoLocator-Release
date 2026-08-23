package com.example.geoguessr_app.domain.model.statistics

data class PlayerStatistics(
    val playerName: String,
    val gamesPlayed: Int,
    val totalScore: Int,
    val bestScore: Int
)