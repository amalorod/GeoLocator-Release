package com.example.geoguessr_app.ui.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoguessr_app.data.firebase.SessionRepository
import com.example.geoguessr_app.domain.model.multiplayer.MatchSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Beobachtet die laufende [MatchSession] sowie den Fortschritt aller
 * Mitspieler während einer aktiven Multiplayer-Partie.
 *
 * Im Gegensatz zu [LobbyViewModel.observeLobby] ist [observeSession] durch
 * [isObserving] explizit gegen mehrfaches, paralleles Abonnieren
 * desselben Firebase-Listeners abgesichert.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState = _uiState.asStateFlow()

    private val _session = MutableStateFlow<MatchSession?>(null)
    val session = _session.asStateFlow()

    private var isObserving = false

    /** Startet die Echtzeit-Beobachtung von Session- und Spielerdaten. */
    fun observeSession(sessionId: String) {
        if (isObserving) return
        isObserving = true

        viewModelScope.launch {
            sessionRepository.observeSession(sessionId).collect { session ->
                _session.value = session
            }
        }

        viewModelScope.launch {
            sessionRepository.observePlayerStates(sessionId).collect { players ->
                _uiState.update {
                    it.copy(
                        sessionId = sessionId,
                        players = players,
                        allPlayersFinished = players.isNotEmpty() && players.all { p -> p.finishedRound }
                    )
                }
            }
        }
    }
}