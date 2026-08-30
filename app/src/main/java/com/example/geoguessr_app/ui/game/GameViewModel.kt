package com.example.geoguessr_app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoguessr_app.data.datastore.StatisticsDataStoreRepository
import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.model.custom.CustomDifficulty
import com.example.geoguessr_app.domain.model.custom.CustomGameSettings
import com.example.geoguessr_app.domain.model.custom.Region
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
import com.example.geoguessr_app.data.dailyquest.DailyQuestRepository
import com.example.geoguessr_app.data.statistics.StatisticsRepository
import com.example.geoguessr_app.domain.model.statistics.MatchStatistic
import com.example.geoguessr_app.domain.statistics.GameStatistics
import com.example.geoguessr_app.domain.statistics.RoundStatistics


/**
 * Verwaltet den Zustand und Ablauf einer vollständigen Partie.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val dailyQuestRepository: DailyQuestRepository,
    private val getRandomLocations: GetRandomLocationsUseCase,
    private val calculateDistance: CalculateDistanceUseCase,
    private val calculateScore: CalculateScoreUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())

    /**
     * Öffentlich ausschließlich lesbarer Spielzustand.
     */
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameLocations: List<GeoLocation> = emptyList()
    private var timerJob: Job? = null
    private var customSettings: CustomGameSettings? = null

    init {
        startNewGame(GameMode.NORMAL)
    }

    /**
     * Startet eine Partie mit individuellen Einstellungen.
     */
    fun startCustomGame(settings: CustomGameSettings) {
        this.customSettings = settings
        loadGame(GameMode.CUSTOM)
    }

    /**
     * Lädt asynchron fünf unterschiedliche Standorte für eine Partie.
     */
    fun startNewGame(gameMode: GameMode) {
        this.customSettings = null
        loadGame(gameMode)
    }

    private fun loadGame(gameMode: GameMode = _uiState.value.gameMode) {
        timerJob?.cancel()

        val duration = customSettings?.timeLimitSeconds ?: gameMode.roundDurationSeconds

        val initialLives = when (customSettings?.difficulty) {
            CustomDifficulty.EASY -> Int.MAX_VALUE
            CustomDifficulty.MEDIUM -> 5
            CustomDifficulty.HARD -> 3
            null -> {
                if (gameMode == GameMode.BATTLE_ROYALE) 3 else Int.MAX_VALUE
            }
        }

        val navEnabled = when (customSettings?.difficulty) {
            CustomDifficulty.HARD -> false
            else -> gameMode.streetViewNavigationEnabled
        }

        _uiState.value = GameUiState(
            gameMode = gameMode,
            remainingSeconds = duration,
            lives = initialLives,
            isStreetViewNavigationEnabled = navEnabled
        )

        viewModelScope.launch {
            runCatching {
                getRandomLocations(
                    count = _uiState.value.totalRounds,
                    region = customSettings?.region ?: Region.WORLD
                )
            }.onSuccess { locations ->
                gameLocations = locations

                val firstLocation = locations.firstOrNull()

                if (firstLocation == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, errorMessage = "Es wurde kein Spielstandort gefunden."
                    )
                    return@onSuccess
                }

                _uiState.value = _uiState.value.copy(
                    currentLocation = firstLocation, isLoading = false
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
     * Speichert den aktuell auf der Weltkarte ausgewählten Tipp.
     *
     * Die Auswahl liegt im ViewModel und bleibt deshalb bei einer
     * Neukomposition oder Gerätdrehung erhalten.
     */
    fun selectGuess(guessedLocation: GeoCoordinate) {
        val state = _uiState.value

        if (state.isLoading || state.isPaused || state.isRoundFinished || state.isGameFinished) {
            return
        }

        _uiState.value = state.copy(
            guessedLocation = guessedLocation
        )
    }

    /**
     * Startet den Countdown der aktuellen Runde.
     *
     * Während einer Pause bleibt die Coroutine aktiv, verändert den
     * Countdown aber nicht. Nach dem Fortsetzen läuft derselbe Timer weiter.
     */
    private fun startTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && !_uiState.value.isRoundFinished) {
                delay(1_000)

                val state = _uiState.value

                if (!state.isPaused && !state.isRoundFinished) {
                    _uiState.value = state.copy(
                        remainingSeconds = state.remainingSeconds - 1
                    )
                }
            }

            if (_uiState.value.remainingSeconds == 0 && !_uiState.value.isRoundFinished) {
                finishRoundWithoutGuess()
            }
        }
    }

    /**
     * Pausiert die aktuelle Partie.
     *
     * Spielfortschritt, Kartenmarker und verbleibende Zeit bleiben erhalten.
     */
    fun pauseGame() {
        val state = _uiState.value

        if (state.isLoading || state.isRoundFinished || state.isGameFinished || state.isPaused) {
            return
        }

        _uiState.value = state.copy(
            isPaused = true
        )
    }

    /**
     * Setzt eine pausierte Partie fort.
     */
    fun resumeGame() {
        val state = _uiState.value

        if (!state.isPaused || state.isGameFinished) {
            return
        }

        _uiState.value = state.copy(
            isPaused = false
        )
    }


    /**
     * Öffnet die Weltkarte, auf der ein Tipp abgegeben werden kann.
     */
    fun showGuessMap() {
        val state = _uiState.value

        if (state.isLoading || state.isPaused || state.isRoundFinished || state.isGameFinished) {
            return
        }

        _uiState.value = state.copy(
            viewMode = GameViewMode.GUESS_MAP
        )
    }

    /**
     * Wechselt von der Tippkarte zurück zur Street-View-Ansicht.
     *
     * Ein bereits gesetzter Kartenmarker bleibt dabei erhalten.
     */
    fun showStreetView() {
        val state = _uiState.value

        if (state.isLoading || state.isRoundFinished || state.isGameFinished) {
            return
        }

        _uiState.value = state.copy(
            viewMode = GameViewMode.STREET_VIEW
        )
    }

    /**
     * Wertet den zuvor auf der Karte ausgewählten Tipp aus.
     */
    fun submitGuess() {
        val state = _uiState.value
        val actualLocation = state.currentLocation ?: return
        val guessedLocation = state.guessedLocation ?: return

        if (state.isLoading || state.isRoundFinished || state.isGameFinished) {
            return
        }

        timerJob?.cancel()

        val actualCoordinate = actualLocation.coordinate

        val distance = calculateDistance(
            actualLocation = actualCoordinate, guessedLocation = guessedLocation
        )

        val score = calculateScore(
            distanceKilometers = distance
        )

        val roundStatisticsEntry = RoundStatistics(
            roundNumber = state.currentRound, score = score, distanceKm = distance
        )

        var newLives = state.lives
        if (state.gameMode == GameMode.CUSTOM && distance > 500.0 && state.lives < 100) {
            newLives = (state.lives - 1).coerceAtLeast(0)
        }

        _uiState.value = state.copy(
            roundDistanceKilometers = distance,
            roundScore = score,
            totalScore = state.totalScore + score,
            lives = newLives,

            isRoundFinished = !state.isMultiplayer,

            waitingForPlayers = state.isMultiplayer,

            roundStatistics = state.roundStatistics + roundStatisticsEntry,
        )

        if (newLives <= 0 && state.gameMode == GameMode.CUSTOM) {
            _uiState.value = _uiState.value.copy(isGameFinished = true)
        }

        checkDailyQuests(distance, score, state.gameMode)
    }

    private fun checkDailyQuests(distance: Double, score: Int, mode: GameMode) {
        viewModelScope.launch {
            // Quest: Präzision (< 25km)
            if (distance < 25.0) {
                val completed = dailyQuestRepository.updateQuestProgress("perfect_guess")
                if (completed != null) _uiState.value =
                    _uiState.value.copy(newlyCompletedQuest = completed)
            }

            // Quest: Europa Experte (< 100km)
            if (distance < 100.0) {
                val completed = dailyQuestRepository.updateQuestProgress("europe_explorer")
                if (completed != null) _uiState.value =
                    _uiState.value.copy(newlyCompletedQuest = completed)
            }

            // Quest: Pro-Modus
            if (mode == GameMode.PRO) {
                val completed = dailyQuestRepository.updateQuestProgress("pro_mode_guess")
                if (completed != null) _uiState.value =
                    _uiState.value.copy(newlyCompletedQuest = completed)
            }
        }
    }

    fun dismissCompletedQuest() {
        _uiState.value = _uiState.value.copy(newlyCompletedQuest = null)
    }

    /**
     * Beendet eine Runde ohne Punkte, wenn das Zeitlimit abläuft.
     */
    private fun finishRoundWithoutGuess() {
        timerJob?.cancel()

        val state = _uiState.value

        val roundStatisticsEntry = RoundStatistics(
            roundNumber = state.currentRound, score = 0, distanceKm = 0.0
        )

        _uiState.value = _uiState.value.copy(
            guessedLocation = null,
            roundDistanceKilometers = null,
            roundScore = 0,
            isRoundFinished = true,
            roundStatistics = state.roundStatistics + roundStatisticsEntry,
        )
    }

    /**
     * Beginnt die nächste Runde oder beendet die Partie.
     */
    fun startNextRound() {
        val state = _uiState.value

        if (!state.isRoundFinished) {
            return
        }

        if (state.currentRound >= state.totalRounds) {

            viewModelScope.launch {
                // Save detailed match statistics
                val match = MatchStatistic(
                    timestamp = System.currentTimeMillis(),
                    gameMode = state.gameMode.name,
                    score = state.totalScore,
                    rounds = state.roundStatistics.size,
                    distanceKm = state.roundStatistics.sumOf { it.distanceKm },
                    won = false,
                    multiplayer = false
                )
                statisticsRepository.saveMatch(match)
            }
            _uiState.value = state.copy(
                isGameFinished = true, gameStatistics = GameStatistics(
                    totalScore = state.totalScore, rounds = state.roundStatistics
                )
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

        val duration = customSettings?.timeLimitSeconds ?: state.gameMode.roundDurationSeconds

        _uiState.value = state.copy(
            currentRound = nextRound,
            remainingSeconds = duration,
            currentLocation = nextLocation,
            isPaused = false,
            guessedLocation = null,
            roundDistanceKilometers = null,
            roundScore = null,
            viewMode = GameViewMode.STREET_VIEW,
            isRoundFinished = false,
            errorMessage = null
        )

        startTimer()
    }

    /**
     * Startet nach einem Ladefehler eine neue Partie.
     */
    fun retryLoading() {
        loadGame(_uiState.value.gameMode)
    }
}
