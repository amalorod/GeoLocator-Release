package com.example.geoguessr_app.ui.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoguessr_app.data.firebase.FirebaseAuthRepository
import com.example.geoguessr_app.data.firebase.SessionRepository
import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState
import com.example.geoguessr_app.domain.usecase.CalculateDistanceUseCase
import com.example.geoguessr_app.domain.usecase.CalculateScoreUseCase
import com.example.geoguessr_app.domain.usecase.GetLocationsByIdsUseCase
import com.example.geoguessr_app.ui.game.GameUiState
import com.example.geoguessr_app.ui.game.GameViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val firebaseAuthRepository: FirebaseAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState(isMultiplayer = true))
    val uiState = _uiState.asStateFlow()

    private var gameLocations: List<GeoLocation> = emptyList()
    private var sessionId: String = ""
    private var playerName: String = "Spieler"
    private var isHost: Boolean = false

    fun loadSessionLocations(sessionId: String) {
        this.sessionId = sessionId
        val currentUid = firebaseAuthRepository.currentUid() ?: return

        // Observe Session for round changes
        viewModelScope.launch {
            sessionRepository.observeSession(sessionId).collect { session ->
                if (session == null) return@collect
                
                isHost = session.hostUid == currentUid
                
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
                        isLoading = false
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

                // Host check: If all players finished round, host can trigger next round
                if (isHost && players.isNotEmpty() && players.all { it.finishedRound && it.round == _uiState.value.currentRound } && !_uiState.value.isGameFinished) {
                    // In einer echten Implementierung könnte hier ein Timer starten 
                    // oder der "Nächste Runde" Button für den Host aktiviert werden.
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

        val actualCoordinate = GeoCoordinate(currentLocation.latitude, currentLocation.longitude)
        val distance = calculateDistance(actualCoordinate, guessedLocation)
        val score = calculateScore(distance)

        val updatedTotalScore = state.totalScore + score

        _uiState.update {
            it.copy(
                totalScore = updatedTotalScore,
                roundScore = score,
                roundDistanceKilometers = distance,
                isRoundFinished = true,
                waitingForPlayers = true
            )
        }

        viewModelScope.launch {
            val uid = firebaseAuthRepository.currentUid() ?: return@launch
            
            // Holen wir den aktuellen State um lives zu bearbeiten
            val currentPlayerState = _uiState.value.multiplayerPlayers.find { it.uid == uid }
            var currentLives = currentPlayerState?.lives ?: 5
            
            // Battle Royale Logic: Leben abziehen bei zu großer Entfernung
            if (distance > 500.0) {
                currentLives--
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
        }
    }

    fun startNextRound() {
        if (!isHost) return // Nur der Host darf die Session-Runde erhöhen
        
        val state = _uiState.value
        if (state.currentRound >= state.totalRounds) {
            // Spiel beenden in Session?
            return
        }

        val nextRound = state.currentRound + 1
        val nextLocation = gameLocations.getOrNull(nextRound - 1) ?: return

        viewModelScope.launch {
            sessionRepository.advanceRound(
                sessionId = sessionId,
                nextRound = nextRound,
                nextLocationId = nextLocation.id
            )
            
            // Host setzt sich selbst auch auf "nicht fertig" für die neue Runde
            // (Andere Spieler werden durch den Observe-Loop in loadSessionLocations zurückgesetzt)
            val uid = firebaseAuthRepository.currentUid() ?: return@launch
            val currentPlayerState = _uiState.value.multiplayerPlayers.find { it.uid == uid }
            
            sessionRepository.updatePlayerState(
                sessionId = sessionId,
                playerState = MultiplayerPlayerState(
                    uid = uid,
                    playerName = playerName,
                    score = state.totalScore,
                    round = nextRound,
                    lives = currentPlayerState?.lives ?: 5,
                    finishedRound = false
                )
            )
        }
    }
}