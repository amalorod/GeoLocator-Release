package com.example.geoguessr_app.domain.model.multiplayer

import com.google.firebase.database.PropertyName

data class LobbyPlayer(
    val uid: String = "",
    val name: String = "",
    @get:PropertyName("isReady")
    @set:PropertyName("isReady")
    var isReady: Boolean = false,
    @get:PropertyName("isHost")
    @set:PropertyName("isHost")
    var isHost: Boolean = false
)

