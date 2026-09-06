package com.example.geoguessr_app.domain.model.multiplayer

import com.example.geoguessr_app.domain.model.GeoLocation

/**
 * Repräsentiert eine laufende Multiplayer-Spielsitzung, die nach dem
 * Start einer Lobby erzeugt wird (siehe Doku, Kapitel 3.4
 * „Spielsitzungen“).
 *
 * Im Gegensatz zu [Lobby], die den Zustand vor Spielbeginn beschreibt,
 * bildet diese Klasse den tatsächlichen Spielfortschritt ab und wird
 * über die gesamte Partie hinweg kontinuierlich per Firebase Realtime
 * Database synchronisiert (siehe Doku 3.3 „Echtzeit-Synchronisation“).
 *
 * @property sessionId Eindeutiger Bezeichner der Sitzung, verknüpft
 *   diese mit der ursprünglichen [Lobby.sessionId].
 * @property hostUid UID des Spielers, der die Partie gestartet hat.
 * @property lobbyCode Code der zugehörigen Lobby, zur Nachverfolgung
 *   der Herkunft der Session.
 * @property mode Gewählter Spielmodus als String (siehe Anmerkung zu
 *   [Lobby.mode] bzgl. [MultiplayerMode]).
 * @property currentRound Aktuelle Rundennummer, beginnend bei 1.
 * @property totalRounds Gesamtzahl der Runden der Partie (Standard: 5,
 *   siehe Doku, Kapitel 1 „Kernfunktionen“: typischerweise 5 Runden pro
 *   Spiel).
 * @property currentLocationId ID des in der aktuellen Runde zu
 *   erratenden Standorts (siehe [GeoLocation.id]).
 * @property roundStartTimestamp Startzeitpunkt der aktuellen Runde in
 *   Millisekunden seit Unix-Epoch; ermöglicht z. B. die Berechnung
 *   verbleibender Zeit im Zeitlimit-Modus.
 * @property started Kennzeichnet, ob die Partie bereits begonnen hat.
 * @property finished Kennzeichnet, ob die Partie beendet ist (z. B.
 *   nach Erreichen von totalRounds oder – im Battle-Royale-Modus –
 *   sobald nur noch ein aktiver Spieler übrig ist, siehe Doku 5.1).
 * @property locationIds Vorab festgelegte, für alle Teilnehmer
 *   identische Liste von Standort-IDs, damit laut Doku (3.4) „alle
 *   Spieler dieselben Standorte erhalten und dadurch dieselbe Partie
 *   spielen“.
 */
data class MatchSession(
    val sessionId: String = "",
    val hostUid: String = "",
    val lobbyCode: String = "",
    val mode: String = "",
    val currentRound: Int = 1,
    val totalRounds: Int = 5,
    val currentLocationId: String = "",
    val roundStartTimestamp: Long = 0L,
    val started: Boolean = false,
    val finished: Boolean = false,
    val locationIds: List<String> = emptyList()
)