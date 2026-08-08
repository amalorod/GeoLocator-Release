package com.example.geoguessr_app.ui.game

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
    val isRoundFinished: Boolean = false,
    val isGameFinished: Boolean = false
)