package com.example.geoguessr_app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.usecase.CalculateDistanceUseCase
import com.example.geoguessr_app.domain.usecase.CalculateScoreUseCase
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
 * Verwaltet Zustand und Ablauf einer vollständigen Partie.
 *
 * Das ViewModel erhält seine Anwendungslogik als Use Cases über Hilt.
 * Dadurch bleiben Distanzberechnung, Punkteberechnung und Standortauswahl
 * unabhängig von Compose und der Benutzeroberfläche testbar.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val getRandomLocations: GetRandomLocationsUseCase,
    private val calculateDistance: CalculateDistanceUseCase,
    private val calculateScore: CalculateScoreUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameLocations: List<GeoLocation> = emptyList()
    private var timerJob: Job? = null

    init {
        loadGame()
    }

    /**
     * Lädt asynchron fünf unterschiedliche Standorte für eine Partie.
     */
    private fun loadGame() {
        timerJob?.cancel()

        _uiState.value = GameUiState()

        viewModelScope.launch {
            runCatching {
                getRandomLocations(count = _uiState.value.totalRounds)
            }.onSuccess { locations ->
                gameLocations = locations

                val firstLocation = locations.firstOrNull()

                if (firstLocation == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Es wurde kein Spielstandort gefunden."
                    )
                    return@onSuccess
                }

                _uiState.value = _uiState.value.copy(
                    currentLocation = firstLocation,
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

    /**
     * Startet den Countdown der aktuellen Runde.
     */
    private fun startTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (
                _uiState.value.remainingSeconds > 0 &&
                !_uiState.value.isRoundFinished
            ) {
                delay(1_000)

                _uiState.value = _uiState.value.copy(
                    remainingSeconds = _uiState.value.remainingSeconds - 1
                )
            }

            if (_uiState.value.remainingSeconds == 0) {
                finishRoundWithoutGuess()
            }
        }
    }

    /**
     * Wertet einen geografischen Tipp aus.
     *
     * Die tatsächliche Position wird aus dem aktuellen Spielstandort
     * übernommen. Entfernung und Punktzahl liefern die Domain-Use-Cases.
     */
    fun submitGuess(guessedLocation: GeoCoordinate) {
        val state = _uiState.value
        val actualLocation = state.currentLocation ?: return

        if (
            state.isLoading ||
            state.isRoundFinished ||
            state.isGameFinished
        ) {
            return
        }

        timerJob?.cancel()

        val actualCoordinate = GeoCoordinate(
            latitude = actualLocation.latitude,
            longitude = actualLocation.longitude
        )

        val distance = calculateDistance(
            actualLocation = actualCoordinate,
            guessedLocation = guessedLocation
        )

        val score = calculateScore(
            distanceKilometers = distance
        )

        _uiState.value = state.copy(
            guessedLocation = guessedLocation,
            roundDistanceKilometers = distance,
            roundScore = score,
            totalScore = state.totalScore + score,
            isRoundFinished = true
        )
    }

    /**
     * Beendet eine Runde ohne Tipp, wenn der Countdown abläuft.
     */
    private fun finishRoundWithoutGuess() {
        timerJob?.cancel()

        _uiState.value = _uiState.value.copy(
            roundDistanceKilometers = null,
            roundScore = 0,
            isRoundFinished = true
        )
    }

    /**
     * Beginnt die nächste Runde oder beendet die Partie.
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

        if (nextLocation == null) {
            _uiState.value = state.copy(
                errorMessage = "Der nächste Standort konnte nicht geladen werden."
            )
            return
        }

        _uiState.value = state.copy(
            currentRound = nextRound,
            remainingSeconds = 60,
            currentLocation = nextLocation,
            guessedLocation = null,
            roundDistanceKilometers = null,
            roundScore = null,
            isRoundFinished = false,
            errorMessage = null
        )

        startTimer()
    }

    /**
     * Startet nach einem Ladefehler eine vollständig neue Partie.
     */
    fun retryLoading() {
        loadGame()
    }
}
