package com.example.geoguessr_app.domain.model.custom

data class CustomGameSettings(
    val timeLimitSeconds: Int = 60,
    val region: Region = Region.WORLD,
    val difficulty: CustomDifficulty = CustomDifficulty.MEDIUM
)
