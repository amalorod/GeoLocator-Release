package com.example.geoguessr_app.data.firebase

import com.example.geoguessr_app.domain.model.multiplayer.Lobby
import com.example.geoguessr_app.domain.model.multiplayer.LobbyPlayer
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class MultiplayerRepository {

    private val database =
        FirebaseDatabase.getInstance()

    suspend fun createLobby(
        lobbyCode: String,
        hostPlayer: LobbyPlayer
    ) {

        val lobby = Lobby(
            lobbyCode = lobbyCode,
            hostUid = hostPlayer.uid,
            players = listOf(hostPlayer)
        )

        database
            .reference
            .child("lobbies")
            .child(lobbyCode)
            .setValue(lobby)
            .await()
    }
}