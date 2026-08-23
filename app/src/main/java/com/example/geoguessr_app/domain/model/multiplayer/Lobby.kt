package com.example.geoguessr_app.domain.model.multiplayer

import com.google.firebase.database.PropertyName

data class Lobby(
    val lobbyCode: String = "",
    val hostUid: String = "",
    val mode: String = "FREEPLAY",
    val players: List<LobbyPlayer> = emptyList(),
    @get:PropertyName("started")
    @set:PropertyName("started")
    var started: Boolean = false,
    val sessionId: String = ""
)