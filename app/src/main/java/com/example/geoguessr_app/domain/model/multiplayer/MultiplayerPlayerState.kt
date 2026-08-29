package com.example.geoguessr_app.domain.model.multiplayer

/**
 * Repräsentiert den laufenden Spielzustand eines einzelnen Spielers
 * innerhalb einer aktiven [MatchSession] (siehe Doku, Kapitel 3.4 und
 * 3.5).
 *
 * Im Gegensatz zu [LobbyPlayer] (Zustand vor Spielbeginn) bildet diese
 * Klasse den fortlaufenden Spielstand während der Partie ab, u. a. für
 * das Live-Scoreboard (siehe Doku, Kapitel 4.2 „Game Screen“).
 *
 * @property uid Firebase-UID des Spielers.
 * @property playerName Anzeigename des Spielers im Scoreboard.
 * @property score Bisher in der Session erzielte Gesamtpunktzahl.
 * @property round Aktuelle Runde, in der sich der Spieler befindet.
 * @property lives Verbleibende Leben im Battle-Royale-Modus (siehe
 *   Doku 5.1: ein Leben wird verloren, wenn die Schätzung mehr als 500
 *   km vom tatsächlichen Ort entfernt liegt); in anderen Modi
 *   voraussichtlich ohne Wirkung.
 * @property finishedRound Kennzeichnet, ob der Spieler seinen Tipp für
 *   die aktuelle Runde bereits abgegeben hat. Wird laut Doku (3.4)
 *   verwendet, um zu erkennen, wann alle Spieler bereit für die
 *   nächste Runde sind.
 * @property currentGuessScore Punktzahl der zuletzt abgegebenen
 *   Schätzung, getrennt von der kumulierten score-Gesamtsumme.
 * @property lastSeenTimestamp Zeitstempel des letzten „Lebenszeichens“
 *   im Rahmen des Heartbeat-Systems (siehe Doku, Kapitel 3.5
 *   „Verbindungsüberwachung“). Bleibt dieser Wert zu lange
 *   unverändert, wird der Spieler als inaktiv erkannt und entfernt.
 */
data class MultiplayerPlayerState(
    val uid: String = "",
    val playerName: String = "",
    val score: Int = 0,
    val round: Int = 1,
    val lives: Int = 5,
    val finishedRound: Boolean = false,
    val currentGuessScore: Int = 0,
    val lastSeenTimestamp: Long = 0L
)