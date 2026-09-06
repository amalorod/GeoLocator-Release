package com.example.geoguessr_app.domain.util

/**
 * Erzeugt eindeutige Kennungen für Multiplayer-Spielsitzungen (siehe
 * [MatchSession.sessionId]).
 *
 * HINWEIS: Die ID basiert auf dem aktuellen Zeitstempel in Millisekunden.
 * Das ist eine simple Umsetzung und für Demozwecke ausreichend, birgt aber
 * theoretisch ein Kollisionsrisiko, falls zwei Sessions innerhalb derselben
 * Millisekunde erzeugt werden.
 *
 * Für den Rahmen dieses Projekts wird das theoretische Kollisionsrisiko bewusst
 * in Kauf genommen, da der Implementierungsaufwand einer garantiert kollisionsfreien
 * Lösung in keinem Verhältnis zum praktischen Nutzen steht. Für die Weiterentwicklung
 * bzw. für die Veröffentlichung der App würde ich auf UUID oder push().key() von
 * Firebase Realtime Database zurückgreifen.
 */
object SessionIdGenerator {

    fun generate(): String {
        return System.currentTimeMillis().toString()
    }
}
