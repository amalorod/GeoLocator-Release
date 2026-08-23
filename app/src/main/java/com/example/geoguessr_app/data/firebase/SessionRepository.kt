package com.example.geoguessr_app.data.firebase

import com.example.geoguessr_app.domain.model.multiplayer.MatchSession
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.*
import kotlinx.coroutines.flow.Flow


class SessionRepository {

    private val database =
        FirebaseDatabase.getInstance()

    suspend fun loadSession(
        sessionId: String
    ): MatchSession? {

        val snapshot =
            database.reference
                .child("sessions")
                .child(sessionId)
                .get()
                .await()

        return snapshot.getValue(
            MatchSession::class.java
        )
    }

    suspend fun createSession(
        session: MatchSession
    ) {

        database.reference
            .child("sessions")
            .child(session.sessionId)
            .setValue(session)
            .await()
    }

    suspend fun updatePlayerState(
        sessionId: String,
        playerState: MultiplayerPlayerState
    ) {

        database.reference
            .child("sessions")
            .child(sessionId)
            .child("players")
            .child(playerState.uid)
            .setValue(playerState)
            .await()
    }



    suspend fun advanceRound(
        sessionId: String,
        nextRound: Int,
        nextLocationId: String
    ) {
        database.reference
            .child("sessions")
            .child(sessionId)
            .child("currentRound")
            .setValue(nextRound)

        database.reference
            .child("sessions")
            .child(sessionId)
            .child("currentLocationId")
            .setValue(nextLocationId)

        database.reference
            .child("sessions")
            .child(sessionId)
            .child("roundStartTimestamp")
            .setValue(
                System.currentTimeMillis()
            )
    }
    fun observePlayerStates(
        sessionId: String
    ): Flow<List<MultiplayerPlayerState>> =
        callbackFlow {

            val reference =
                database.reference
                    .child("sessions")
                    .child(sessionId)
                    .child("players")

            val listener =
                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        val players =
                            snapshot.children.mapNotNull {

                                it.getValue(
                                    MultiplayerPlayerState::class.java
                                )
                            }

                        trySend(players)
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {
                    }
                }

            reference.addValueEventListener(
                listener
            )

            awaitClose {

                reference.removeEventListener(
                    listener
                )
            }
        }

    fun observeSession(
        sessionId: String
    ): Flow<MatchSession?> = callbackFlow {

        val reference =
            database.reference
                .child("sessions")
                .child(sessionId)

        val listener =
            object : ValueEventListener {

                override fun onDataChange(
                    snapshot: DataSnapshot
                ) {

                    val session =
                        snapshot.getValue(
                            MatchSession::class.java
                        )

                    trySend(session)
                }

                override fun onCancelled(
                    error: DatabaseError
                ) {
                }
            }

        reference.addValueEventListener(
            listener
        )

        awaitClose {

            reference.removeEventListener(
                listener
            )
        }
    }

}
