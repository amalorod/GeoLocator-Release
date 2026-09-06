package com.example.geoguessr_app.domain.statistics

import com.example.geoguessr_app.domain.usecase.CalculateDistanceUseCase
import com.example.geoguessr_app.domain.usecase.CalculateScoreUseCase

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
 *   im UI beide Werte separat anzeigen zu können.
 */
data class RoundStatistics(
    val roundNumber: Int,
    val score: Int,
    val distanceKm: Double
)