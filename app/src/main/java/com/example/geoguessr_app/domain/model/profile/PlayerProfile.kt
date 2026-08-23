package com.example.geoguessr_app.domain.model.profile

data class PlayerProfile(
    val playerId: String,
    val playerName: String,
    val profileImageUrl: String? = null
)
