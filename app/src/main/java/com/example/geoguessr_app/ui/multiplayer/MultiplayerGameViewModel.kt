package com.example.geoguessr_app.ui.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoguessr_app.data.dailyquest.DailyQuestRepository
import com.example.geoguessr_app.data.firebase.FirebaseAuthRepository
import com.example.geoguessr_app.data.firebase.SessionRepository
import com.example.geoguessr_app.data.profile.ProfileRepository
import com.example.geoguessr_app.data.statistics.StatisticsRepository
import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerMode
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState
import com.example.geoguessr_app.domain.model.statistics.MatchStatistic
import com.example.geoguessr_app.domain.usecase.CalculateDistanceUseCase
import com.example.geoguessr_app.domain.usecase.CalculateScoreUseCase
import com.example.geoguessr_app.domain.usecase.GetLocationsByIdsUseCase
import com.example.geoguessr_app.ui.game.GameMode
import com.example.geoguessr_app.ui.game.GameUiState
import com.example.geoguessr_app.ui.game.GameViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MultiplayerGameViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val getLocationsByIds: GetLocationsByIdsUseCase,
    private val calculateDistance: CalculateDistanceUseCase,
    private val calculateScore: CalculateScoreUseCase,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val statisticsRepository: StatisticsRepository,
    private val dailyQuestRepository: DailyQuestRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState(isMultiplayer = true))
    val uiState = _uiState.asStateFlow()

    private var gameLocations: List<GeoLocation> = emptyList()
    private var sessionId: String = ""
    private var playerName: String = "Spieler"
    private var isHost: Boolean = false
    private var heartbeatJob: Job? = null

    fun loadSessionLocations(sessionId: String) {
        this.sessionId = sessionId
        val currentUid = firebaseAuthRepository.currentUid() ?: return
        
        startHeartbeat()

        // Observe Session for round changes
        viewModelScope.launch {
            sessionRepository.observeSession(sessionId).collect { session ->
                if (session == null) return@collect
                
                isHost = session.hostUid == currentUid

                // Map Session Mode to GameMode
                val currentMode = when(session.mode) {
                    "BATTLE_ROYALE" -> GameMode.BATTLE_ROYALE
                    "PRO" -> GameMode.PRO
                    else -> GameMode.MULTIPLAYER
                }

                val myState = _uiState.value.multiplayerPlayers.find { it.uid == currentUid }
                val isEliminated = myState != null && myState.lives <= 0

                if ((session.finished || isEliminated) && !_uiState.value.isGameFinished) {
                    _uiState.update { it.copy(isGameFinished = true, gameMode = currentMode) }
                    saveMatchResult()
                    return@collect
                }
                
                if (gameLocations.isEmpty()) {
                    val locations = getLocationsByIds(session.locationIds)
                    gameLocations = locations
                }

                val nextLocation = gameLocations.find { it.id == session.currentLocationId }
                
                if (session.currentRound != _uiState.value.currentRound || 
                    _uiState.value.currentLocation?.id != session.currentLocationId) {
                    
                    _uiState.update { it.copy(
                        currentRound = session.currentRound,
                        currentLocation = nextLocation,
                        totalRounds = session.totalRounds,
                        isRoundFinished = false,
                        guessedLocation = null,
                        roundScore = null,
                        roundDistanceKilometers = null,
                        viewMode = GameViewMode.STREET_VIEW,
                        isLoading = false,
                        gameMode = currentMode
                    ) }

                    // Wenn die Runde gewechselt hat, setzen wir unseren eigenen Status zurück,
                    // falls wir nicht der Host sind (Host hat es schon beim Wechsel getan)
                    if (!isHost) {
                        viewModelScope.launch {
                            val uid = firebaseAuthRepository.currentUid() ?: return@launch
                            // Wir holen uns die aktuellen Leben aus dem UI State (da dort die Liste der Spieler ist)
                            val currentPlayer = _uiState.value.multiplayerPlayers.find { it.uid == uid }
                            
                            sessionRepository.updatePlayerState(
                                sessionId = sessionId,
                                playerState = MultiplayerPlayerState(
                                    uid = uid,
                                    playerName = playerName,
                                    score = _uiState.value.totalScore,
                                    round = session.currentRound,
                                    lives = currentPlayer?.lives ?: 5,
                                    finishedRound = false
                                )
                            )
                        }
                    }
                }
            }
        }

        // Observe Players for Scoreboard and Host check
        viewModelScope.launch {
            sessionRepository.observePlayerStates(sessionId).collect { players ->
                playerName = players.find { it.uid == currentUid }?.playerName ?: "Spieler"
                
                _uiState.update { it.copy(multiplayerPlayers = players) }

                // Auto-Win check: Wenn man alleine übrig ist (und es war eine Multiplayer-Runde)
                if (players.size == 1 && !_uiState.value.isGameFinished) {
                    _uiState.update { it.copy(isGameFinished = true) }
                    saveMatchResult()
                }

                // Disconnect check (Host only)
                if (isHost) {
                    val now = System.currentTimeMillis()
                    val inactivePlayers = players.filter { 
                        it.uid != currentUid && (now - it.lastSeenTimestamp > 20000) 
                    }
                    inactivePlayers.forEach { inactive ->
                        viewModelScope.launch {
                            sessionRepository.removePlayerFromSession(sessionId, inactive.uid)
                        }
                    }
                }

                // Host check: If all players finished round, host can trigger next round
                val allPlayersFinished = players.isNotEmpty() && players.all { it.finishedRound && it.round == _uiState.value.currentRound }
                
                if (isHost && allPlayersFinished && !_uiState.value.isGameFinished && _uiState.value.isRoundFinished) {
                    // Kurze Verzögerung damit man das Ergebnis sehen kann
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(3000)
                        startNextRound()
                    }
                }
            }
        }
    }

    fun selectGuess(guessedLocation: GeoCoordinate) {
        if (_uiState.value.isRoundFinished) return
        _uiState.update { it.copy(guessedLocation = guessedLocation) }
    }

    fun showGuessMap() {
        _uiState.update { it.copy(viewMode = GameViewMode.GUESS_MAP) }
    }

    fun showStreetView() {
        _uiState.update { it.copy(viewMode = GameViewMode.STREET_VIEW) }
    }

    fun submitGuess() {
        val state = _uiState.value
        val currentLocation = state.currentLocation ?: return
        val guessedLocation = state.guessedLocation ?: return

        val actualCoordinate = currentLocation.coordinate
        val distance = calculateDistance(actualCoordinate, guessedLocation)
        val score = calculateScore(distance)

        val updatedTotalScore = state.totalScore + score

        viewModelScope.launch {
            val uid = firebaseAuthRepository.currentUid() ?: return@launch
            val currentPlayerState = state.multiplayerPlayers.find { it.uid == uid }
            var currentLives = currentPlayerState?.lives ?: 5
            
            // Battle Royale Logic: Leben abziehen bei > 500km
            if (state.gameMode == GameMode.BATTLE_ROYALE && distance > 500.0) {
                currentLives = (currentLives - 1).coerceAtLeast(0)
            }

            _uiState.update {
                it.copy(
                    totalScore = updatedTotalScore,
                    roundScore = score,
                    roundDistanceKilometers = distance,
                    isRoundFinished = true,
                    waitingForPlayers = true
                )
            }

            sessionRepository.updatePlayerState(
                sessionId = sessionId,
                playerState = MultiplayerPlayerState(
                    uid = uid,
                    playerName = playerName,
                    score = updatedTotalScore,
                    round = state.currentRound,
                    lives = currentLives,
                    finishedRound = true,
                    currentGuessScore = score
                )
            )
            
            checkDailyQuests(distance, score, state.gameMode)
        }
    }

    private fun checkDailyQuests(distance: Double, score: Int, mode: GameMode) {
        viewModelScope.launch {
            if (distance < 25.0) {
                val completed = dailyQuestRepository.updateQuestProgress("perfect_guess")
                if (completed != null) _uiState.update { it.copy(newlyCompletedQuest = completed) }
            }
            if (distance < 100.0) {
                val completed = dailyQuestRepository.updateQuestProgress("europe_explorer")
                if (completed != null) _uiState.update { it.copy(newlyCompletedQuest = completed) }
            }
            if (mode == GameMode.PRO) {
                val completed = dailyQuestRepository.updateQuestProgress("pro_mode_guess")
                if (completed != null) _uiState.update { it.copy(newlyCompletedQuest = completed) }
            }
        }
    }

    fun dismissCompletedQuest() {
        _uiState.update { it.copy(newlyCompletedQuest = null) }
    }

    fun startNextRound() {
        if (!isHost) return // Nur der Host darf die Session-Runde erhöhen
        
        val state = _uiState.value

        viewModelScope.launch {
            // Check if game should end
            val survivors = state.multiplayerPlayers.filter { it.lives > 0 }
            if (state.currentRound >= state.totalRounds || (state.gameMode == GameMode.BATTLE_ROYALE && survivors.size <= 1)) {
                sessionRepository.finishSession(sessionId)
                return@launch
            }

            val nextRound = state.currentRound + 1
            val nextLocation = gameLocations.getOrNull(nextRound - 1) ?: return@launch

            sessionRepository.advanceRound(
                sessionId = sessionId,
                nextRound = nextRound,
                nextLocationId = nextLocation.id
            )
            
            // Alle Spieler (die noch dabei sind) zurücksetzen
            state.multiplayerPlayers.filter { it.lives > 0 }.forEach { p ->
                sessionRepository.updatePlayerState(
                    sessionId = sessionId,
                    playerState = p.copy(
                        round = nextRound,
                        finishedRound = false
                    )
                )
            }
        }
    }

    fun leaveGame() {
        viewModelScope.launch {
            val currentUid = firebaseAuthRepository.currentUid() ?: return@launch
            sessionRepository.removePlayerFromSession(sessionId, currentUid)
        }
    }

    private fun saveMatchResult() {
        heartbeatJob?.cancel()
        val state = _uiState.value
        val myState = state.multiplayerPlayers.find { 
            it.uid == firebaseAuthRepository.currentUid() 
        }
        val won = myState != null && myState.lives > 0 && 
                 (state.currentRound >= state.totalRounds || 
                  state.multiplayerPlayers.count { it.lives > 0 } <= 1)

            val match = MatchStatistic(
                timestamp = System.currentTimeMillis(),
                gameMode = state.gameMode.name,
                score = state.totalScore,
                rounds = state.currentRound,
                distanceKm = state.roundDistanceKilometers ?: 0.0, // This might need refinement if multiple rounds are summed
                won = won,
                multiplayer = true
            )
        viewModelScope.launch {
            statisticsRepository.saveMatch(match)
            
            // Quest: Multiplayer Win
            if (won) {
                val completed = dailyQuestRepository.updateQuestProgress("multiplayer_win")
                if (completed != null) _uiState.update { it.copy(newlyCompletedQuest = completed) }
                
                // Quest: Battle Royale Win
                if (state.gameMode == GameMode.BATTLE_ROYALE) {
                    val brCompleted = dailyQuestRepository.updateQuestProgress("battle_royale_win")
                    if (brCompleted != null) _uiState.update { it.copy(newlyCompletedQuest = brCompleted) }
                }
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (true) {
                val uid = firebaseAuthRepository.currentUid() ?: break
                val state = _uiState.value
                val profile = profileRepository.profile.value
                val currentName = profile?.playerName ?: playerName
                val currentPlayer = state.multiplayerPlayers.find { it.uid == uid }
                
                sessionRepository.updatePlayerState(
                    sessionId = sessionId,
                    playerState = MultiplayerPlayerState(
                        uid = uid,
                        playerName = currentName,
                        score = state.totalScore,
                        round = state.currentRound,
                        lives = currentPlayer?.lives ?: 5,
                        finishedRound = state.isRoundFinished,
                        currentGuessScore = state.roundScore ?: 0,
                        lastSeenTimestamp = System.currentTimeMillis()
                    )
                )
                delay(10000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        heartbeatJob?.cancel()
    }
}
