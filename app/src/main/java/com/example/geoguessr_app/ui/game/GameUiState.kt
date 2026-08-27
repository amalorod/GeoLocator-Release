package com.example.geoguessr_app.ui.game

import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState
import com.example.geoguessr_app.domain.model.statistics.RoundStatistics
import com.example.geoguessr_app.domain.model.statistics.GameStatistics

/**
 * Beschreibt den vollständigen sichtbaren Zustand des Spiels.
 *
 * Das ViewModel erzeugt bei jeder Änderung mit copy() eine neue Instanz.
 * Dadurch bleibt der Zustand unveränderlich und nachvollziehbar.
 */
data class GameUiState(
    val currentRound: Int = 1,
    val totalRounds: Int = 5,
    val remainingSeconds: Int = 60,
    val totalScore: Int = 0,
    val currentLocation: GeoLocation? = null,
    val guessedLocation: GeoCoordinate? = null,
    val roundDistanceKilometers: Double? = null,
    val roundScore: Int? = null,
    val viewMode: GameViewMode = GameViewMode.STREET_VIEW,
    val isPaused: Boolean = false,
    val isLoading: Boolean = true,
    val isRoundFinished: Boolean = false,
    val isGameFinished: Boolean = false,
    val errorMessage: String? = null,
    val gameMode: GameMode = GameMode.NORMAL,
    val roundStatistics: List<RoundStatistics> = emptyList(),
    val gameStatistics: GameStatistics? = null,
    val isMultiplayer: Boolean = false,
    val sessionId: String? = null,
    val waitingForPlayers: Boolean = false,

    val multiplayerPlayers:
    List<MultiplayerPlayerState> =
        emptyList(),
    val newlyCompletedQuest: com.example.geoguessr_app.domain.model.dailyquest.DailyQuest? = null,
    val lives: Int = Int.MAX_VALUE,
    val isStreetViewNavigationEnabled: Boolean = true
)