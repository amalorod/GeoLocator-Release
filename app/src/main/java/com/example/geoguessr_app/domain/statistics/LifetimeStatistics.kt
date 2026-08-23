package com.example.geoguessr_app.domain.model.statistics

data class LifetimeStatistics(
    val gamesPlayed: Int = 0,
    val roundsPlayed: Int = 0,
    val totalScore: Int = 0,
    val bestGameScore: Int = 0,
    val totalDistanceKm: Double = 0.0
)