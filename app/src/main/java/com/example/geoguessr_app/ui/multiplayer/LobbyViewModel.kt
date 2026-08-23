package com.example.geoguessr_app.ui.multiplayer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoguessr_app.data.firebase.FirebaseAuthRepository
import com.example.geoguessr_app.data.firebase.MultiplayerRepository
import com.example.geoguessr_app.data.firebase.SessionRepository
import com.example.geoguessr_app.domain.model.multiplayer.LobbyPlayer
import com.example.geoguessr_app.domain.model.multiplayer.MatchSession
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerMode
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState
import com.example.geoguessr_app.domain.usecase.GetRandomLocationsUseCase
import com.example.geoguessr_app.domain.util.LobbyCodeGenerator
import com.example.geoguessr_app.domain.util.SessionIdGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val multiplayerRepository: MultiplayerRepository,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val sessionRepository: SessionRepository,
    private val getRandomLocations: GetRandomLocationsUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(LobbyUiState())

    val uiState: StateFlow<LobbyUiState> =
        _uiState.asStateFlow()

    init {
        Log.e("MULTIPLAYER", "LobbyViewModel INIT")
    }

    fun selectMode(mode: MultiplayerMode) {
        _uiState.update { it.copy(selectedMode = mode, errorMessage = null) }
    }

    fun createLobby() {
        Log.e("MULTIPLAYER", "createLobby() START")
        viewModelScope.launch {
            try {
                // 1. Sofort Code generieren und UI anzeigen
                val code = LobbyCodeGenerator.generate()
                Log.d("MULTIPLAYER", "Lobby Code generated: $code")
                
                _uiState.update { 
                    it.copy(
                        lobbyCode = code,
                        isLoading = true,
                        errorMessage = null
                    ) 
                }

                // 2. Auth sicherstellen
                val uid = firebaseAuthRepository.currentUid()
                    ?: firebaseAuthRepository.signInAnonymously()
                Log.e("MULTIPLAYER", "UID = $uid")

                val hostPlayer = LobbyPlayer(
                    uid = uid,
                    name = "Alic",
                    ready = true,
                    host = true
                )

                _uiState.update { 
                    it.copy(
                        lobbyCode = code,
                        isLoading = true,
                        currentUserUid = uid,
                        errorMessage = null
                    ) 
                }

                // 3. In Firebase speichern
                multiplayerRepository.createLobby(
                    lobbyCode = code,
                    hostPlayer = hostPlayer
                )
                
                // 4. Beobachten und finaler State-Update
                observeLobby(code)

                _uiState.update {
                    it.copy(
                        players = listOf(hostPlayer),
                        isLoading = false
                    )
                }
                Log.d("MULTIPLAYER", "Lobby created successfully: $code")
            } catch (e: Exception) {
                Log.e("MULTIPLAYER", "Error creating lobby", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun joinLobby(
        lobbyCode: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, lobbyCode = lobbyCode, errorMessage = null) }

                // 1. Prüfen, ob Lobby existiert
                if (!multiplayerRepository.lobbyExists(lobbyCode)) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Lobby '$lobbyCode' wurde nicht gefunden."
                        )
                    }
                    return@launch
                }

                // 2. Auth sicherstellen
                val uid = firebaseAuthRepository.currentUid()
                    ?: firebaseAuthRepository.signInAnonymously()

                _uiState.update { it.copy(currentUserUid = uid) }

                // 3. Beitreten
                multiplayerRepository.joinLobby(
                    lobbyCode = lobbyCode,
                    player = LobbyPlayer(
                        uid = uid,
                        name = "Spieler",
                        ready = false,
                        host = false
                    )
                )

                observeLobby(lobbyCode)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e("MULTIPLAYER", "Error joining lobby", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Fehler beim Beitreten.") }
            }
        }
    }

    fun leaveLobby() {
        val code = _uiState.value.lobbyCode
        val uid = _uiState.value.currentUserUid
        if (code.isEmpty() || uid.isEmpty()) return

        viewModelScope.launch {
            multiplayerRepository.leaveLobby(code, uid)
        }
    }

    fun toggleReady() {
        val code = _uiState.value.lobbyCode
        val uid = _uiState.value.currentUserUid
        if (code.isEmpty() || uid.isEmpty()) return

        viewModelScope.launch {
            multiplayerRepository.toggleReadyStatus(code, uid)
        }
    }

    fun observeLobby(lobbyCode: String) {
        viewModelScope.launch {
            multiplayerRepository
                .observeLobby(lobbyCode)
                .collect { lobby ->
                    Log.d("MULTIPLAYER", "Observed Lobby: $lobby")
                    if (lobby != null) {
                        _uiState.update {
                            it.copy(
                                lobbyCode = lobby.lobbyCode.ifEmpty { lobbyCode },
                                players = lobby.players,
                                started = lobby.started,
                                sessionId = lobby.sessionId
                            )
                        }
                    }
                }
        }
    }

    fun startLobby() {
        val players = _uiState.value.players
        if (players.size < 2) {
            _uiState.update { it.copy(errorMessage = "Zu wenige Spieler! Mindestens 2 Spieler benötigt") }
            return
        }

        if (!players.all { it.ready }) {
            _uiState.update { it.copy(errorMessage = "Nicht alle Spieler sind bereit!") }
            return
        }

        viewModelScope.launch {
            val code = _uiState.value.lobbyCode
            val sessionId = SessionIdGenerator.generate()
            
            // 1. Standorte für alle Spieler festlegen
            val locations = getRandomLocations(count = 5)
            val locationIds = locations.map { it.id }

            // 2. Session Objekt erstellen
            val session = MatchSession(
                sessionId = sessionId,
                hostUid = firebaseAuthRepository.currentUid() ?: "",
                lobbyCode = code,
                mode = _uiState.value.selectedMode.name,
                locationIds = locationIds,
                totalRounds = 5,
                currentLocationId = locationIds.firstOrNull() ?: ""
            )

            // 3. Session in Firebase anlegen
            sessionRepository.createSession(session)

            // 4. Spieler initialisieren
            _uiState.value.players.forEach { player ->
                sessionRepository.updatePlayerState(
                    sessionId = sessionId,
                    playerState = MultiplayerPlayerState(
                        uid = player.uid,
                        playerName = player.name,
                        score = 0,
                        round = 1
                    )
                )
            }

            // 5. Lobby starten
            multiplayerRepository.startLobby(
                lobbyCode = code,
                sessionId = sessionId
            )
        }
    }





}
