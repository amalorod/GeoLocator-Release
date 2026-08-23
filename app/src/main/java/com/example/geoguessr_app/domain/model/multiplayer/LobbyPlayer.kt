package com.example.geoguessr_app.domain.model.multiplayer

data class LobbyPlayer(
    val uid: String = "",
    val name: String = "",
    val ready: Boolean = false,
    val host: Boolean = false
)
