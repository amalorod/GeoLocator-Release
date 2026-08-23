package com.example.geoguessr_app.ui.multiplayer

import com.example.geoguessr_app.domain.model.multiplayer.LobbyPlayer
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerMode

data class LobbyUiState(
    val lobbyCode: String = "",
    val isLoading: Boolean = false,
    val currentUserUid: String = "",
    val players: List<LobbyPlayer> = emptyList(),
    val selectedMode: MultiplayerMode =
        MultiplayerMode.FREEPLAY,
    val started: Boolean = false,
    val sessionId: String = "",
    val errorMessage: String? = null
)