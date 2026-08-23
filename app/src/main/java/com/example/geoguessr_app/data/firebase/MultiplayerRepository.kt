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

    // BITTE PRÜFE DIESE URL IN DER FIREBASE CONSOLE!
    private val DB_URL = "https://bsi-geoguessr-app-63b7f-default-rtdb.europe-west1.firebasedatabase.app/"

    private val database = FirebaseDatabase.getInstance(DB_URL)

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

            Log.e("MULTIPLAYER", "Versuche Schreibvorgang an: ${database.reference.child("lobbies").child(lobbyCode)}")

            database.reference
                .child("lobbies")
                .child(lobbyCode)
                .setValue(lobby)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.e("MULTIPLAYER", "Schreibvorgang ERFOLGREICH")
                    } else {
                        Log.e("MULTIPLAYER", "Schreibvorgang FEHLGESCHLAGEN: ${task.exception?.message}")
                    }
                }
                .await()

            Log.e("MULTIPLAYER", "LOBBY GESPEICHERT (await beendet)")

        } catch (e: Exception) {
            Log.e("MULTIPLAYER", "EXCEPTION", e)
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

        // Nur hinzufügen, wenn noch nicht drin
        if (lobby.players.none { it.uid == player.uid }) {
            val updatedPlayers = lobby.players + player
            lobbyRef.child("players").setValue(updatedPlayers).await()
        }
    }

    suspend fun leaveLobby(
        lobbyCode: String,
        uid: String
    ) {
        val lobbyRef = database.reference
            .child("lobbies")
            .child(lobbyCode)

        val snapshot = lobbyRef.get().await()
        val lobby = snapshot.getValue(Lobby::class.java) ?: return

        val updatedPlayers = lobby.players.filter { it.uid != uid }

        if (updatedPlayers.isEmpty()) {
            // Letzter Spieler geht -> Lobby löschen
            lobbyRef.removeValue().await()
        } else {
            // Wenn der Host geht, neuen Host ernennen (den ersten in der Liste)
            val finalPlayers = if (lobby.hostUid == uid) {
                updatedPlayers.mapIndexed { index, p ->
                    if (index == 0) p.copy(host = true) else p
                }
            } else {
                updatedPlayers
            }

            val newHostUid = if (lobby.hostUid == uid) finalPlayers.first().uid else lobby.hostUid

            val updates = mapOf(
                "players" to finalPlayers,
                "hostUid" to newHostUid
            )
            lobbyRef.updateChildren(updates).await()
        }
    }

    suspend fun toggleReadyStatus(
        lobbyCode: String,
        uid: String
    ) {
        val lobbyRef = database.reference
            .child("lobbies")
            .child(lobbyCode)

        val snapshot = lobbyRef.get().await()
        val lobby = snapshot.getValue(Lobby::class.java) ?: return

        val updatedPlayers = lobby.players.map { player ->
            if (player.uid == uid) {
                player.copy(ready = !player.ready)
            } else {
                player
            }
        }

        lobbyRef.child("players").setValue(updatedPlayers).await()
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
