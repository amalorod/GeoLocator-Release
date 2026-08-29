package com.example.geoguessr_app.domain.util

/**
 * Erzeugt eindeutige Kennungen für Multiplayer-Spielsitzungen (siehe
 * [MatchSession.sessionId]).
 *
 * ARCHITEKTUR-HINWEIS: Die ID basiert auf dem aktuellen Zeitstempel in
 * Millisekunden. Das ist einfach und in der Praxis für dieses Projekt
 * ausreichend, birgt aber theoretisch ein Kollisionsrisiko, falls zwei
 * Sessions innerhalb derselben Millisekunde erzeugt werden (z. B. bei
 * zwei Hosts, die nahezu gleichzeitig ein Spiel starten). Eine robustere
 * Alternative wäre die Verwendung von Firebase-generierten Push-IDs
 * (push().key) oder einer UUID, die eine praktisch kollisionsfreie
 * Eindeutigkeit garantieren.
 */
object SessionIdGenerator {

    fun generate(): String {
        return System.currentTimeMillis().toString()
    }
}