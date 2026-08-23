package com.example.geoguessr_app.domain.model.multiplayer

data class LobbyPlayer(
    val uid: String = "",
    val name: String = "",
    val isReady: Boolean = false,
    val isHost: Boolean = false
)

