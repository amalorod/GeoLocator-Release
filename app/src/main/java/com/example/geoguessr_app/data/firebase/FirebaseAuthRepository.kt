package com.example.geoguessr_app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Kapselt den Zugriff auf Firebase Authentication.
 *
 * Ist die alleinige Stelle in der Anwendung, die direkt mit der
 * FirebaseAuth-API interagiert. Alle Repositories, die den
 * Anmeldestatus benötigen (z. B. ProfileRepository), erhalten diese
 * Klasse per Konstruktor-Injektion, statt selbst eine eigene
 * FirebaseAuth-Instanz zu halten.
 */
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {

    /**
     * Meldet den Nutzer anonym bei Firebase an und liefert die dabei
     * erzeugte oder bereits bestehende UID zurück.
     *
     * @throws IllegalStateException falls Firebase entgegen Erwartung
     *   keine gültige UID liefert.
     */
    suspend fun signInAnonymously(): String {
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: throw IllegalStateException(
            "UID konnte nicht erzeugt werden."
        )
    }

    /**
     * Liefert die UID des aktuell angemeldeten Nutzers, oder null,
     * falls keine aktive Firebase-Sitzung besteht.
     */
    fun currentUid(): String? = auth.currentUser?.uid

    /**
     * Beendet die aktuelle Firebase-Sitzung vollständig.
     *
     * WICHTIG: Dies ist die zentrale Stelle für unseren zuvor
     * erarbeiteten "echter Logout"-Fix (siehe ProfileRepository.logout).
     * Firebase Anonymous Authentication persistiert eine Sitzung sonst
     * geräteweit über App-Neustarts hinweg; erst dieser explizite
     * Aufruf sorgt dafür, dass ein Nutzer nach einem bewussten Logout
     * beim nächsten App-Start tatsächlich abgemeldet bleibt, analog zu
     * klassischen Login-Systemen wie Online-Banking.
     */
    fun signOut() {
        auth.signOut()
    }
}