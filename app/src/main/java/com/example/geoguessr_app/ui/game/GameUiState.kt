package com.example.geoguessr_app.ui.game

import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.example.geoguessr_app.domain.model.GeoLocation

/**
 * Vollständiger sichtbarer Zustand des Spielbildschirms.
 *
 * Das ViewModel verändert keine einzelnen Eigenschaften direkt,
 * sondern erzeugt mit copy() jeweils einen neuen Zustand.
 */
data class GameUiState(
    val currentRound: Int = 1,
    val totalRounds: Int = 5,
    val remainingSeconds: Int = 60,
    val totalScore: Int = 0,
    val currentLocation: GeoLocation? = null,
    val guessedLocation: GeoCoordinate? = null,
    val roundDistanceKilometers: Double? = null,
    val roundScore: Int? = null,
    val isLoading: Boolean = true,
    val isRoundFinished: Boolean = false,
    val isGameFinished: Boolean = false,
    val errorMessage: String? = null
)