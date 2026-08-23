package com.example.geoguessr_app.ui.multiplayer

import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState

data class SessionUiState(

    val sessionId: String = "",

    val players:
    List<MultiplayerPlayerState> =
        emptyList(),

    val allPlayersFinished: Boolean = false,
)