package com.example.geoguessr_app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.usecase.GetRandomLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Verwaltet den Zustand und Ablauf einer vollständigen Partie.
 *
 * Das ViewModel kennt weder Compose-Komponenten noch die konkrete
 * Repository-Implementierung. Zufällige Standorte erhält es über
 * einen Use Case der Domain-Schicht.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val getRandomLocations: GetRandomLocationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameLocations: List<GeoLocation> = emptyList()
    private var timerJob: Job? = null

    init {
        loadGame()
    }

    /**
     * Lädt asynchron fünf unterschiedliche Zufallsstandorte.
     */
    private fun loadGame() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                getRandomLocations(count = _uiState.value.totalRounds)
            }.onSuccess { locations ->
                gameLocations = locations

                _uiState.value = _uiState.value.copy(
                    currentLocation = locations.firstOrNull(),
                    isLoading = false
                )

                startTimer()
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                        ?: "Die Standorte konnten nicht geladen werden."
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && !_uiState.value.isRoundFinished) {
                delay(1_000)

                _uiState.value = _uiState.value.copy(
                    remainingSeconds = _uiState.value.remainingSeconds - 1
                )
            }

            if (_uiState.value.remainingSeconds == 0) {
                finishRound()
            }
        }
    }

    /**
     * Simuliert weiterhin einen Tipp mit 1.000 Punkten.
     *
     * Im nächsten Entwicklungsschritt ersetzen wir dies durch
     * Koordinatenauswahl, Distanz- und Punkteberechnung.
     */
    fun submitGuess() {
        val state = _uiState.value

        if (
            state.isLoading ||
            state.isRoundFinished ||
            state.isGameFinished ||
            state.currentLocation == null
        ) {
            return
        }

        _uiState.value = state.copy(
            totalScore = state.totalScore + 1_000
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
     * Wählt anhand der Rundennummer den nächsten zuvor
     * zufällig bestimmten Standort aus.
     */
    fun startNextRound() {
        val state = _uiState.value

        if (!state.isRoundFinished) return

        if (state.currentRound >= state.totalRounds) {
            _uiState.value = state.copy(
                isGameFinished = true
            )
            return
        }

        val nextRound = state.currentRound + 1
        val nextLocation = gameLocations.getOrNull(nextRound - 1)

        _uiState.value = state.copy(
            currentRound = nextRound,
            remainingSeconds = 60,
            currentLocation = nextLocation,
            isRoundFinished = false
        )

        startTimer()
    }

    fun retryLoading() {
        timerJob?.cancel()
        loadGame()
    }
}
