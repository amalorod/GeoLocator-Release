package com.example.geoguessr_app.domain.model.multiplayer

/**
 * Repräsentiert einen einzelnen Spieler innerhalb einer [Lobby].
 *
 * @property uid Firebase-Authentication-UID des Spielers, eindeutig
 *   über alle Lobbys und Sessions hinweg.
 * @property name Anzeigename des Spielers (siehe Doku 3.2 – zeigt
 *   Klarnamen statt technischer IDs in der Lobby-Übersicht).
 * @property ready Bereitschaftsstatus im Ready-System (siehe Doku,
 *   Kapitel 3.3). Der Host kann das Spiel erst starten, sobald alle
 *   Spieler ready == true gesetzt haben.
 * @property host Kennzeichnet, ob dieser Spieler aktuell der Host der
 *   Lobby ist. Redundant zu [Lobby.hostUid], erleichtert aber die
 *   direkte Anzeige eines Host-Symbols pro Spieler in der UI, ohne
 *   dass zusätzlich mit lobby.hostUid verglichen werden muss.
 */
data class LobbyPlayer(
    val uid: String = "",
    val name: String = "",
    val ready: Boolean = false,
    val host: Boolean = false
)