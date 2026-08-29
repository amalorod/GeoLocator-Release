package com.example.geoguessr_app.domain.statistics

/**
 * Repräsentiert das Ergebnis einer einzelnen Spielrunde.
 *
 * @property roundNumber Nummer der Runde innerhalb der Partie
 *   (beginnend bei 1).
 * @property score Erzielte Punktzahl dieser Runde (siehe
 *   [CalculateScoreUseCase]).
 * @property distanceKm Entfernung zwischen geschätztem und
 *   tatsächlichem Standort in Kilometern (siehe
 *   [CalculateDistanceUseCase]), zusätzlich zum Score gespeichert, um
 *   im UI beide Werte separat anzeigen zu können (z. B. auch als
 *   Bananen-Maßstab, siehe Aufgabenstellung Kernfunktionen).
 */
data class RoundStatistics(
    val roundNumber: Int,
    val score: Int,
    val distanceKm: Double
)