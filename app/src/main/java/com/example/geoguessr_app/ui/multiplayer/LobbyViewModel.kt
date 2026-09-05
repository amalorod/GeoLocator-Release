package com.example.geoguessr_app.ui.multiplayer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoguessr_app.data.firebase.FirebaseAuthRepository
import com.example.geoguessr_app.data.firebase.MultiplayerRepository
import com.example.geoguessr_app.data.firebase.SessionRepository
import com.example.geoguessr_app.data.profile.ProfileRepository
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

/**
 * Steuert die Lobby-Phase: Erstellen, Beitreten, Ready-Status und den
 * Übergang zur laufenden Partie via [startLobby].
 *
 * BEKANNTER PUNKT (zurückgestellt): [observeLobby] besitzt aktuell keinen
 * Schutz gegen mehrfaches gleichzeitiges Beobachten derselben Lobby
 * (im Gegensatz zu [SessionViewModel.observeSession] mit seinem
 * isObserving-Flag). Solange [createLobby]/[joinLobby] nur einmal pro
 * Lobby-Beitritt aufgerufen werden, ist das unkritisch.
 */
@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val multiplayerRepository: MultiplayerRepository,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val getRandomLocations: GetRandomLocationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    /** Wechselt den Spielmodus; wirkt nur, wenn der lokale Nutzer Host ist. */
    fun selectMode(mode: MultiplayerMode) {
        val lobbyCode = _uiState.value.lobbyCode
        val isHost = _uiState.value.players
            .find { it.uid == _uiState.value.currentUserUid }?.host ?: false

        if (!isHost) return

        _uiState.update { it.copy(selectedMode = mode, errorMessage = null) }

        viewModelScope.launch {
            multiplayerRepository.updateLobbyMode(lobbyCode, mode.name)
        }
    }

    /**
     * Erzeugt eine neue Lobby mit dem lokalen Nutzer als Host.
     *
     * Fehler werden hier bewusst nur geloggt, ohne [LobbyUiState.errorMessage]
     * zu setzen: Das Erstellen einer Lobby gilt als praktisch immer
     * erfolgreich (keine Nutzereingabe, die fehlschlagen könnte, im
     * Gegensatz zu [joinLobby] mit einem potenziell falsch eingegebenen Code).
     */
    fun createLobby() {
        viewModelScope.launch {
            try {
                val code = LobbyCodeGenerator.generate()

                _uiState.update {
                    it.copy(lobbyCode = code, isLoading = true, errorMessage = null)
                }

                val uid = firebaseAuthRepository.currentUid()
                    ?: firebaseAuthRepository.signInAnonymously()

                val profile = profileRepository.profile.value
                val hostPlayer = LobbyPlayer(
                    uid = uid, name = profile?.playerName ?: "Host", ready = true, host = true
                )

                _uiState.update {
                    it.copy(
                        lobbyCode = code,
                        isLoading = true,
                        currentUserUid = uid,
                        errorMessage = null
                    )
                }

                multiplayerRepository.createLobby(lobbyCode = code, hostPlayer = hostPlayer)

                observeLobby(code)

                _uiState.update { it.copy(players = listOf(hostPlayer), isLoading = false) }
            } catch (e: Exception) {
                Log.e("MULTIPLAYER", "Error creating lobby", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /** Tritt einer bestehenden Lobby anhand des Codes bei. */
    fun joinLobby(lobbyCode: String) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(isLoading = true, lobbyCode = lobbyCode, errorMessage = null)
                }

                if (!multiplayerRepository.lobbyExists(lobbyCode)) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Lobby '$lobbyCode' wurde nicht gefunden."
                        )
                    }
                    return@launch
                }

                val uid = firebaseAuthRepository.currentUid()
                    ?: firebaseAuthRepository.signInAnonymously()

                _uiState.update { it.copy(currentUserUid = uid) }

                val profile = profileRepository.profile.value
                multiplayerRepository.joinLobby(
                    lobbyCode = lobbyCode,
                    player = LobbyPlayer(
                        uid = uid,
                        name = profile?.playerName ?: "Spieler",
                        ready = false,
                        host = false
                    )
                )

                observeLobby(lobbyCode)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e("MULTIPLAYER", "Error joining lobby", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Fehler beim Beitreten.")
                }
            }
        }
    }

    /** Entfernt den lokalen Nutzer aus der aktuellen Lobby. */
    fun leaveLobby() {
        val code = _uiState.value.lobbyCode
        val uid = _uiState.value.currentUserUid
        if (code.isEmpty() || uid.isEmpty()) return

        viewModelScope.launch {
            multiplayerRepository.leaveLobby(code, uid)
        }
    }

    /** Schaltet den Ready-Status des lokalen Nutzers um. */
    fun toggleReady() {
        val code = _uiState.value.lobbyCode
        val uid = _uiState.value.currentUserUid
        if (code.isEmpty() || uid.isEmpty()) return

        viewModelScope.launch {
            multiplayerRepository.toggleReadyStatus(code, uid)
        }
    }

    /**
     * Abonniert Echtzeit-Änderungen der Lobby (neue/entfernte Spieler,
     * Ready-Status, Moduswechsel, Spielstart).
     */
    fun observeLobby(lobbyCode: String) {
        viewModelScope.launch {
            multiplayerRepository.observeLobby(lobbyCode).collect { lobby ->
                if (lobby != null) {
                    val remoteMode = try {
                        MultiplayerMode.valueOf(lobby.mode)
                    } catch (e: Exception) {
                        MultiplayerMode.FREEPLAY
                    }

                    _uiState.update {
                        it.copy(
                            lobbyCode = lobby.lobbyCode.ifEmpty { lobbyCode },
                            players = lobby.players,
                            started = lobby.started,
                            sessionId = lobby.sessionId,
                            selectedMode = remoteMode
                        )
                    }
                }
            }
        }
    }

    /**
     * Startet die Partie: legt eine [MatchSession] mit für alle Spieler
     * identischen Standorten an, initialisiert den Spielstand jedes
     * Mitspielers und markiert die Lobby anschließend als gestartet.
     *
     * Bricht die Session-Initialisierung eines einzelnen Spielers ab,
     * wird die zuvor angelegte Session wieder gelöscht, statt eine
     * halb-initialisierte, verwaiste Session in Firebase zu hinterlassen.
     */
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

            try {
                val locations = getRandomLocations(count = 5)
                val locationIds = locations.map { it.id }

                val initialLives =
                    if (_uiState.value.selectedMode == MultiplayerMode.BATTLE_ROYALE) 3 else 5

                val initialPlayers = _uiState.value.players.map { player ->
                    MultiplayerPlayerState(
                        uid = player.uid,
                        playerName = player.name,
                        score = 0,
                        round = 1,
                        lives = initialLives
                    )
                }

                val session = MatchSession(
                    sessionId = sessionId,
                    hostUid = firebaseAuthRepository.currentUid() ?: "",
                    lobbyCode = code,
                    mode = _uiState.value.selectedMode.name,
                    locationIds = locationIds,
                    totalRounds = 5,
                    currentLocationId = locationIds.firstOrNull() ?: ""
                )

                try {
                    sessionRepository.createSessionWithPlayers(session, initialPlayers)
                } catch (e: Exception) {
                    Log.d("MULTIPLAYER", "Fehler bei Session-Init mit Spielern", e)
                    sessionRepository.deleteSession(sessionId)
                    _uiState.update { it.copy(errorMessage = "Partie konnte nicht gestartet werden. Bitte erneut versuchen.") }
                    return@launch
                }

                multiplayerRepository.startLobby(lobbyCode = code, sessionId = sessionId)
            } catch (e: Exception) {
                Log.e("MULTIPLAYER", "FEHLER in startLobby", e)
                _uiState.update { it.copy(errorMessage = "Partie konnte nicht gestartet werden. Bitte erneut versuchen.") }
            }
        }
    }
}