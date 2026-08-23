package com.example.geoguessr_app.domain.model.multiplayer

data class Lobby(
    val lobbyCode: String = "",
    val hostUid: String = "",
    val mode: String = "FREEPLAY",
    val players: List<LobbyPlayer> = emptyList(),
    val started: Boolean = false
)