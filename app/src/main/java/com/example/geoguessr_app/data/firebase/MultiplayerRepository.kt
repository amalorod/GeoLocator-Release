package com.example.geoguessr_app.data.firebase

import android.util.Log
import com.example.geoguessr_app.domain.model.multiplayer.Lobby
import com.example.geoguessr_app.domain.model.multiplayer.LobbyPlayer
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MultiplayerRepository {

    private val database = FirebaseDatabase.getInstance()

    suspend fun createLobby(
        lobbyCode: String,
        hostPlayer: LobbyPlayer
    ) {
        Log.e("MULTIPLAYER", "Repo: createLobby($lobbyCode)")
        try {
            val lobby = Lobby(
                lobbyCode = lobbyCode,
                hostUid = hostPlayer.uid,
                players = listOf(hostPlayer)
            )

            database.reference
                .child("lobbies")
                .child(lobbyCode)
                .setValue(lobby)
                .await()

            Log.e("MULTIPLAYER", "LOBBY GESPEICHERT")

        } catch (e: Exception) {
            Log.e("MULTIPLAYER", "FIREBASE FEHLER", e)
        }
    }

    suspend fun joinLobby(
        lobbyCode: String,
        player: LobbyPlayer
    ) {
        Log.e("MULTIPLAYER", "Repo: joinLobby($lobbyCode)")
        val lobbyRef = database.reference
            .child("lobbies")
            .child(lobbyCode)

        val snapshot = lobbyRef.get().await()

        val lobby = snapshot.getValue(Lobby::class.java) ?: return

        val updatedPlayers = lobby.players + player

        lobbyRef
            .child("players")
            .setValue(updatedPlayers)
            .await()
    }

    suspend fun lobbyExists(
        code: String
    ): Boolean {
        Log.e("MULTIPLAYER", "Repo: lobbyExists($code)")
        val snapshot = database.reference
            .child("lobbies")
            .child(code)
            .get()
            .await()

        return snapshot.exists()
    }

    fun observeLobby(
        lobbyCode: String
    ): Flow<Lobby?> = callbackFlow {
        val reference = database.reference
            .child("lobbies")
            .child(lobbyCode)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lobby = snapshot.getValue(Lobby::class.java)
                trySend(lobby)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
        }
    }

    suspend fun startLobby(
        lobbyCode: String,
        sessionId: String
    ) {
        try {
            val updates = mapOf(
                "started" to true,
                "sessionId" to sessionId
            )

            database.reference
                .child("lobbies")
                .child(lobbyCode)
                .updateChildren(updates)
                .await()
            
            Log.e("MULTIPLAYER", "LOBBY GESTARTET")
        } catch (e: Exception) {
            Log.e("MULTIPLAYER", "FIREBASE FEHLER BEIM STARTEN", e)
        }
    }
}
