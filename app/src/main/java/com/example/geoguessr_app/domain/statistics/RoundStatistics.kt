package com.example.geoguessr_app.domain.model.statistics

data class RoundStatistics(
    val roundNumber: Int,
    val score: Int,
    val distanceKm: Double
)