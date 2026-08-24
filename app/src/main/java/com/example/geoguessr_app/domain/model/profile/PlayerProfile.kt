package com.example.geoguessr_app.domain.model.profile

data class PlayerProfile(
    val playerId: String = "",
    val playerName: String = "Spieler",
    val profileImageUrl: String? = null,
    val createdAt: Long = 0L
)
