package com.example.geoguessr_app.data.firebase

import android.util.Log
import com.example.geoguessr_app.domain.model.multiplayer.Lobby
import com.example.geoguessr_app.domain.model.multiplayer.LobbyPlayer
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Verwaltet Multiplayer-Lobbys in Firebase Realtime Database (siehe
 * Doku, Kapitel 3.1 „Lobbys erstellen“ und 3.2 „Lobbys beitreten“).
 *
 * Die FirebaseDatabase-Instanz wird zentral über FirebaseModule
 * bereitgestellt (siehe di/-Package) und per Konstruktor injiziert,
 * statt hier eine eigene, redundante Instanz zu erzeugen.
 */
class MultiplayerRepository @Inject constructor(
    private val database: FirebaseDatabase
) {

    /**
     * Erstellt eine neue Lobby mit dem übergebenen Code und setzt den
     * erstellenden Spieler als initialen Host ein.
     *
     * addOnCompleteListener() und das anschließende .await() greifen
     * auf denselben Task zu. Dadurch sind Erfolgs-/Fehler-Logs im Listener
     * redundant zum umgebenden try-catch-Block, welcher Fehler
     * bereits über die geworfene Exception beim await() abfängt.
     */
    suspend fun createLobby(
        lobbyCode: String, hostPlayer: LobbyPlayer
    ) {
        Log.d("MULTIPLAYER", "Repo: createLobby($lobbyCode)")
        try {
            val lobby = Lobby(
                lobbyCode = lobbyCode,
                hostUid = hostPlayer.uid,
                players = mapOf(hostPlayer.uid to hostPlayer)
            )

            database.reference.child("lobbies").child(lobbyCode).setValue(lobby).await()
        } catch (e: Exception) {
            Log.d("MULTIPLAYER", "EXCEPTION", e)
        }
    }

    /**
     * Fügt einen Spieler einer bestehenden Lobby hinzu, sofern er nicht
     * bereits Teil der Spielerliste ist (Schutz vor Duplikaten bei
     * mehrfachem Beitrittsversuch, z. B. durch Netzwerk-Retries).
     */
    suspend fun joinLobby(
        lobbyCode: String, player: LobbyPlayer
    ): Boolean {
        val playersRef = database.reference.child("lobbies").child(lobbyCode).child("players")

        return suspendCancellableCoroutine { continuation ->
            playersRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val playersMap = currentData.value as? Map<*, *> ?: emptyMap<String, Any>()

                    if (playersMap.containsKey(player.uid)) {
                        return Transaction.success(currentData)
                    }
                    if (playersMap.size >= 4) {
                        return Transaction.abort()
                    }

                    currentData.child(player.uid).value = mapOf(
                        "uid" to player.uid,
                        "name" to player.name,
                        "ready" to player.ready,
                        "host" to player.host
                    )
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    continuation.resume(committed) {}
                }
            })
        }
    }

    /**
     * Entfernt einen Spieler aus der Lobby. Verlässt der letzte Spieler
     * die Lobby, wird sie vollständig gelöscht, statt als leere Lobby
     * in der Datenbank zu verbleiben. Verlässt der Host die Lobby,
     * wird automatisch der erste verbleibende Spieler in der Liste zum
     * neuen Host ernannt, damit die Lobby weiterhin steuerbar bleibt.
     *
     * * siehe Doku, Kapitel 3.2 Lobby beitreten
     */
    suspend fun leaveLobby(
        lobbyCode: String, uid: String
    ) {
        val lobbyRef = database.reference.child("lobbies").child(lobbyCode)
        val snapshot = lobbyRef.get().await()
        val lobby = snapshot.getValue(Lobby::class.java) ?: return

        val remainingPlayers = lobby.players - uid

        if (remainingPlayers.isEmpty()) {
            lobbyRef.removeValue().await()
        } else {
            if (lobby.hostUid == uid) {
                val newHostUid = remainingPlayers.keys.first()
                val updates = mapOf(
                    "players/$uid" to null,
                    "players/$newHostUid/host" to true,
                    "hostUid" to newHostUid
                )
                lobbyRef.updateChildren(updates).await()
            } else {
                lobbyRef.child("players").child(uid).removeValue().await()
            }
        }
    }

    /**
     * Kehrt den Bereit-Status (ready) eines Spielers um. Wird
     * verwendet, damit alle Spieler in der Lobby signalisieren können,
     * dass sie zum Spielstart bereit sind siehe [MultiplayerLobbyScreen].
     */
    suspend fun toggleReadyStatus(
        lobbyCode: String, uid: String
    ) {
        val playerRef = database.reference.child("lobbies").child(lobbyCode)
            .child("players").child(uid)
        val snapshot = playerRef.get().await()
        val player = snapshot.getValue(LobbyPlayer::class.java) ?: return

        playerRef.child("ready").setValue(!player.ready).await()
    }

    /**
     * Registriert eine serverseitige Aufräumaktion: Verliert der Client die
     * Verbindung, dann entfernt Firebase den Spieler automatisch aus der Lobby,
     * ohne dass der Client selbst noch aktiv werden muss (siehe Doku 3.2 „Leave-System“).
     *
     * Muss nach createLobby()/joinLobby() aufgerufen werden, da onDisconnect()
     * pro aktiver Socket-Verbindung neu gesetzt werden muss.
     */
    fun registerLobbyPresence(lobbyCode: String, uid: String) {
        val playerRef = database.reference.child("lobbies").child(lobbyCode)
            .child("players").child(uid)
        val connectedRef = database.reference.child(".info/connected")

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue(Boolean::class.java) == true) {
                    playerRef.onDisconnect().removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Prüft, ob ein Lobby-Code tatsächlich einer existierenden Lobby
     * entspricht. Wird beim manuellen Beitreten über einen eingegebenen
     * Code verwendet, um Nutzern eine klare Fehlermeldung bei einem
     * ungültigen Code anzuzeigen (siehe [JoinLobbyScreen]).
     */
    suspend fun lobbyExists(code: String): Boolean {
        val snapshot = database.reference.child("lobbies").child(code).get().await()
        return snapshot.exists()
    }

    /**
     * Liefert einen Live-Datenstrom des Lobby-Zustands. Wird verwendet,
     * damit alle Teilnehmer in Echtzeit sehen, wenn neue Spieler
     * beitreten, den Bereit-Status ändern oder der Host die Partie
     * startet.
     *
     * ANMERKUNG: onCancelled() bleibt hier bewusst leer. Das führt dazu,
     * dass ein Fehler bei der Firebase-Verbindung (z. B. Berechtigungsproblem)
     * aktuell stillschweigend ignoriert werden würde, statt den Flow mit einem
     * Fehler zu terminieren. Im Erweiterungshorizont könnte man ein
     * close(error.toException()) nutzen.
     *
     * Die Änderung in observeLobby() alleine ist zwar trivial, benötigt aber
     * an jeder Aufrufstelle (LobbyViewModel, SessionViewModel bei observeSession())
     * korrektes Error-Handling, was teilweise durch das Heartbeat-System in
     * [com.example.geoguessr_app.ui.multiplayer.MultiplayerGameViewModel]
     * bereits abgefangen wird.
     */
    fun observeLobby(lobbyCode: String): Flow<Lobby?> = callbackFlow {
        val reference = database.reference.child("lobbies").child(lobbyCode)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Lobby::class.java))
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }

    /**
     * Markiert die Lobby als gestartet und verknüpft sie mit der
     * zugehörigen Session-ID, wodurch alle Lobby-Teilnehmer über
     * observeLobby() automatisch zum Spielbildschirm wechseln (siehe
     * [GeoGuessrNavHost]).
     */
    suspend fun startLobby(lobbyCode: String, sessionId: String) {
        try {
            val updates = mapOf(
                "started" to true, "sessionId" to sessionId
            )
            database.reference.child("lobbies").child(lobbyCode).updateChildren(updates).await()
        } catch (e: Exception) {
            Log.d("MULTIPLAYER", "FIREBASE FEHLER BEIM STARTEN", e)
        }
    }

    /**
     * Aktualisiert den gewählten Spielmodus der Lobby (z. B. Anzahl
     * Runden, Region), damit alle Teilnehmer denselben Modus sehen.
     */
    suspend fun updateLobbyMode(lobbyCode: String, mode: String) {
        try {
            database.reference.child("lobbies").child(lobbyCode).child("mode").setValue(mode)
                .await()
        } catch (e: Exception) {
            Log.d("MULTIPLAYER", "Fehler beim Modus-Update", e)
        }
    }
}