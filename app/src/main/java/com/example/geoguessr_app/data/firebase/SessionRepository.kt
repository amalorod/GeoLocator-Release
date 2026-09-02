package com.example.geoguessr_app.data.firebase

import android.util.Log
import com.example.geoguessr_app.domain.model.multiplayer.MatchSession
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Verwaltet die laufende Multiplayer-Partie (MatchSession) in Firebase
 * Realtime Database, nachdem eine Lobby gestartet wurde (siehe
 * MultiplayerRepository.startLobby und Doku, Kapitel 3.4
 * „Spielsitzungen“).
 *
 * Während [MultiplayerRepository] den Zustand vor Spielstart
 * (Lobby-Bildung, Bereit-Status) verwaltet, ist diese Klasse für den
 * eigentlichen Spielverlauf zuständig: Rundenfortschritt, individuelle
 * Spielerzustände und Spielende.
 */
class SessionRepository @Inject constructor(
    private val database: FirebaseDatabase
) {

    /**
     * Lädt den aktuellen Zustand einer Spielsitzung einmalig (kein
     * Live-Abo), z. B. beim erstmaligen Betreten des
     * Multiplayer-Spielbildschirms.
     */
    suspend fun loadSession(sessionId: String): MatchSession? {
        val snapshot = database.reference.child("sessions").child(sessionId).get().await()
        return snapshot.getValue(MatchSession::class.java)
    }

    /**
     * Legt eine neue Spielsitzung in Firebase an. Wird vom Host
     * aufgerufen, sobald er die Lobby startet (siehe
     * MultiplayerRepository.startLobby).
     */
    suspend fun createSession(session: MatchSession) {
        try {
            database.reference.child("sessions").child(session.sessionId).setValue(session)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("MULTIPLAYER", "Session-Schreibvorgang erfolgreich")
                    } else {
                        Log.e(
                            "MULTIPLAYER",
                            "Session-Schreibvorgang fehlgeschlagen: ${task.exception?.message}"
                        )
                    }
                }.await()
        } catch (e: Exception) {
            // Fängt Fehler ab, falls Firebase das Schreiben blockiert
            // (z. B. wegen Security Rules).
            Log.e("MULTIPLAYER", "Fehler beim Erstellen der Session", e)
        }
    }

    /**
     * Löscht eine Spielsitzung vollständig aus Firebase.
     *
     * Wird aktuell ausschließlich als Cleanup in [LobbyViewModel.startLobby]
     * verwendet: Schlägt die Initialisierung der Spielerzustände nach
     * erfolgreichem [createSession] fehl, verhindert dieser Aufruf, dass eine
     * halb-initialisierte, verwaiste Session in Firebase zurückbleibt.
     */
    suspend fun deleteSession(sessionId: String) {
        database.reference.child("sessions").child(sessionId).removeValue().await()
    }

    /**
     * Aktualisiert den individuellen Spielzustand (z. B. aktueller
     * Score, abgegebener Tipp) eines einzelnen Spielers innerhalb der
     * Sitzung.
     */
    suspend fun updatePlayerState(sessionId: String, playerState: MultiplayerPlayerState) {
        database.reference.child("sessions").child(sessionId).child("players")
            .child(playerState.uid).setValue(playerState).await()
    }

    /**
     * Entfernt einen Spieler vollständig aus der Sitzung, z. B. wenn
     * er die laufende Partie vorzeitig verlässt.
     */
    suspend fun removePlayerFromSession(sessionId: String, uid: String) {
        database.reference.child("sessions").child(sessionId).child("players").child(uid)
            .removeValue().await()
    }

    /**
     * Markiert die Sitzung als beendet, sobald alle Runden gespielt
     * wurden.
     */
    suspend fun finishSession(sessionId: String) {
        database.reference.child("sessions").child(sessionId).child("finished").setValue(true)
            .await()
    }

    /**
     * Schaltet die Sitzung auf die nächste Runde weiter: aktualisiert
     * Rundennummer, den neuen Standort sowie den Startzeitpunkt der
     * Runde, damit alle Teilnehmer synchron in dieselbe neue Runde
     * wechseln.
     *
     * ANMERKUNG: Die drei setValue()-Aufrufe werden hier nicht
     * awaited und nicht zu einer einzigen updateChildren()-Transaktion
     * zusammengefasst (im Gegensatz zu z. B. startLobby in
     * MultiplayerRepository). Das bedeutet, die drei Werte könnten in
     * seltenen Fällen kurzfristig inkonsistent zueinander bei anderen
     * Clients ankommen. Eine Vereinheitlichung zu einer einzigen
     * updateChildren()-Transaktion (analog zu startLobby) würde das
     * beheben.
     */
    suspend fun advanceRound(sessionId: String, nextRound: Int, nextLocationId: String) {
        database.reference.child("sessions").child(sessionId).child("currentRound")
            .setValue(nextRound)

        database.reference.child("sessions").child(sessionId).child("currentLocationId")
            .setValue(nextLocationId)

        database.reference.child("sessions").child(sessionId).child("roundStartTimestamp")
            .setValue(System.currentTimeMillis())
    }

    /**
     * Liefert einen Live-Datenstrom aller Spielerzustände innerhalb
     * einer Sitzung, damit z. B. Punktestände anderer Spieler in
     * Echtzeit im UI aktualisiert werden können.
     */
    fun observePlayerStates(sessionId: String): Flow<List<MultiplayerPlayerState>> = callbackFlow {
        val reference = database.reference.child("sessions").child(sessionId).child("players")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = snapshot.children.mapNotNull {
                    it.getValue(MultiplayerPlayerState::class.java)
                }
                trySend(players)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }

    /**
     * Liefert einen Live-Datenstrom des gesamten Sitzungszustands,
     * z. B. um Rundenwechsel oder das Sitzungsende bei allen Clients
     * synchron sichtbar zu machen.
     */
    fun observeSession(sessionId: String): Flow<MatchSession?> = callbackFlow {
        val reference = database.reference.child("sessions").child(sessionId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(MatchSession::class.java))
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }
}