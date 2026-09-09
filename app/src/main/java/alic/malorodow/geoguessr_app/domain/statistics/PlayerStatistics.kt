package alic.malorodow.geoguessr_app.domain.statistics

import alic.malorodow.geoguessr_app.ui.profile.ProfileScreen

/**
 * Repräsentiert die Statistik eines Spielers im Leaderboard, welches über
 * den [ProfileScreen] aufgerufen werden kann.
 *
 * HINWEIS: Diese Klasse überschneidet sich inhaltlich mit
 * [LifetimeStatistics] (beide erfassen gamesPlayed, totalScore sowie
 * einen Bestwert).
 *
 * Der Unterschied liegt darin, dass [LifetimeStatistics]  die eigenen,
 * privaten Statistiken im StatisticsScreen beschreibt, während
 * [PlayerStatistics] für eine Rangliste mehrerer Spieler für das Leaderboard
 * gedacht ist.
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