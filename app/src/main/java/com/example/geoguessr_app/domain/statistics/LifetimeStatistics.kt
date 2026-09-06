package com.example.geoguessr_app.domain.statistics

import com.example.geoguessr_app.domain.model.statistics.MatchStatistic

/**
 * Aggregiert die Statistik eines Spielers über dessen gesamte
 * Nutzungshistorie hinweg (siehe Doku, Kapitel 4.4 „Statistics
 * Screen“).
 *
 * Im Gegensatz zu [GameStatistics] (eine einzelne Partie) bildet diese
 * Klasse die langfristig in Firebase gespeicherte, kumulierte Bilanz
 * ab und wird aus den [MatchStatistic]-Einträgen nach jeder abgeschlossenen
 * Partie aktualisiert.
 *
 * @property gamesPlayed Gesamtzahl gespielter Partien.
 * @property roundsPlayed Gesamtzahl gespielter Einzelrunden über alle
 *   Partien hinweg.
 * @property totalScore Über alle Partien aufsummierte Gesamtpunktzahl.
 * @property bestGameScore Höchste in einer einzelnen Partie erzielte
 *   Gesamtpunktzahl.
 * @property totalDistanceKm Über alle Runden aufsummierte
 *   Entfernungsabweichung in Kilometern; dient als weiterer
 *   langfristiger Leistungsindikator neben der reinen Punktzahl.
 */
data class LifetimeStatistics(
    val gamesPlayed: Int = 0,
    val roundsPlayed: Int = 0,
    val totalScore: Int = 0,
    val bestGameScore: Int = 0,
    val totalDistanceKm: Double = 0.0
)