package com.example.geoguessr_app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Verwaltet die Spiellogik unabhängig von der Benutzeroberfläche.
 *
 * Hilt erstellt das ViewModel. StateFlow liefert den aktuellen Zustand
 * reaktiv an die Compose-Oberfläche.
 */
@HiltViewModel
class GameViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())

    /**
     * Von außen nur lesbarer Zustand.
     */
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    /**
     * Startet einen Coroutine-basierten Countdown.
     *
     * viewModelScope beendet die Coroutine automatisch,
     * sobald das ViewModel endgültig zerstört wird.
     */
    private fun startTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1_000)

                _uiState.value = _uiState.value.copy(
                    remainingSeconds = _uiState.value.remainingSeconds - 1
                )
            }

            finishRound()
        }
    }

    /**
     * Simuliert vorerst die Abgabe eines Tipps.
     *
     * Später wird die feste Punktzahl durch die echte
     * Entfernungs- und Punkteberechnung ersetzt.
     */
    fun submitGuess() {
        if (_uiState.value.isRoundFinished) return

        _uiState.value = _uiState.value.copy(
            totalScore = _uiState.value.totalScore + 1_000
        )

        finishRound()
    }

    private fun finishRound() {
        timerJob?.cancel()

        _uiState.value = _uiState.value.copy(
            isRoundFinished = true
        )
    }

    /**
     * Beginnt die nächste Runde oder beendet das Spiel
     * nach der fünften Runde.
     */
    fun startNextRound() {
        val state = _uiState.value

        if (state.currentRound >= state.totalRounds) {
            _uiState.value = state.copy(
                isGameFinished = true
            )
            return
        }

        _uiState.value = state.copy(
            currentRound = state.currentRound + 1,
            remainingSeconds = 60,
            isRoundFinished = false
        )

        startTimer()
    }
}