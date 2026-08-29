package com.example.geoguessr_app.domain.statistics

/**
 * Repräsentiert die Statistik eines Spielers im Kontext eines
 * Leaderboards bzw. Vergleichs mit anderen Spielern.
 *
 * ARCHITEKTUR-HINWEIS: Diese Klasse überschneidet sich inhaltlich mit
 * [LifetimeStatistics] (beide erfassen gamesPlayed, totalScore sowie
 * einen Bestwert). Der Unterschied liegt vermutlich im
 * Verwendungskontext: [LifetimeStatistics] beschreibt die eigenen,
 * privaten Statistiken im Statistics Screen, während
 * [PlayerStatistics] für eine Rangliste mehrerer Spieler (inkl.
 * playerName) gedacht ist, z. B. für ein zukünftiges Leaderboard-
 * Feature. Sollte dieser Anwendungsfall aktuell nicht umgesetzt sein,
 * ist diese Klasse ein guter Kandidat für den Erweiterungshorizont
 * (Ranglisten-Feature) statt für aktiven Code.
 *
 * @property playerName Anzeigename des Spielers.
 * @property gamesPlayed Anzahl gespielter Partien.
 * @property totalScore Kumulierte Gesamtpunktzahl.
 * @property bestScore Beste jemals erzielte Punktzahl.
 */
data class PlayerStatistics(
    val playerName: String,
    val gamesPlayed: Int,
    val totalScore: Int,
    val bestScore: Int
)