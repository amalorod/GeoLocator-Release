package com.example.geoguessr_app.domain.model.statistics

data class GameStatistics(
    val totalScore: Int,
    val rounds: List<RoundStatistics>
)