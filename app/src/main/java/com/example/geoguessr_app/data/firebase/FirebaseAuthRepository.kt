package com.example.geoguessr_app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository {

    private val auth = FirebaseAuth.getInstance()

    suspend fun signInAnonymously(): String {

        val result =
            auth.signInAnonymously()
                .await()

        return result.user?.uid
            ?: throw IllegalStateException(
                "UID konnte nicht erzeugt werden."
            )
    }

    fun currentUid(): String? =
        auth.currentUser?.uid
}