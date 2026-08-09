package com.example.geoguessr_app.ui.game


import com.example.geoguessr_app.domain.model.GeoLocation

/**
 * Vollständiger sichtbarer Zustand des Spielbildschirms.
 *
 * Die Klasse ist unveränderlich. Bei Änderungen erzeugt das ViewModel
 * mithilfe von copy() einen neuen Zustand.
 */


data class GameUiState(
    val currentRound: Int = 1,
    val totalRounds: Int = 5,
    val remainingSeconds: Int = 60,
    val totalScore: Int = 0,
    val currentLocation: GeoLocation? = null,
    val isLoading: Boolean = true,
    val isRoundFinished: Boolean = false,
    val isGameFinished: Boolean = false,
    val errorMessage: String? = null
)