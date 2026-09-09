package alic.malorodow.geoguessr_app.ui.multiplayer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import alic.malorodow.geoguessr_app.data.dailyquest.DailyQuestRepository
import alic.malorodow.geoguessr_app.data.firebase.FirebaseAuthRepository
import alic.malorodow.geoguessr_app.data.firebase.SessionRepository
import alic.malorodow.geoguessr_app.data.profile.ProfileRepository
import alic.malorodow.geoguessr_app.data.statistics.StatisticsRepository
import alic.malorodow.geoguessr_app.domain.model.GeoCoordinate
import alic.malorodow.geoguessr_app.domain.model.GeoLocation
import alic.malorodow.geoguessr_app.domain.model.custom.Region
import alic.malorodow.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState
import alic.malorodow.geoguessr_app.domain.model.statistics.MatchStatistic
import alic.malorodow.geoguessr_app.domain.statistics.RoundStatistics
import alic.malorodow.geoguessr_app.domain.usecase.CalculateDistanceUseCase
import alic.malorodow.geoguessr_app.domain.usecase.CalculateScoreUseCase
import alic.malorodow.geoguessr_app.domain.usecase.GetLocationsByIdsUseCase
import alic.malorodow.geoguessr_app.ui.game.GameMode
import alic.malorodow.geoguessr_app.ui.game.GameUiState
import alic.malorodow.geoguessr_app.ui.game.GameViewMode
import alic.malorodow.geoguessr_app.ui.game.GameViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Steuert die laufende Multiplayer-Partie nach Spielbeginn: Rundenwechsel,
 * eigene Tipp-Abgabe, Sieger-/Verlierer-Ermittlung sowie das
 * Heartbeat-System zur Erkennung inaktiver Mitspieler.
 *
 * Nutzt bewusst denselben [GameUiState] wie der Einzelspieler-[GameViewModel]
 * (statt eines eigenen Multiplayer-spezifischen States), damit die
 * zustandslose GameScreen-Oberfläche für beide Modi identisch bleibt – die
 * Multiplayer-spezifischen Felder, wie etwa [GameUiState.multiplayerPlayers],
 * [GameUiState.isMultiplayer]) werden nur hier befüllt.
 *
 * Rollenverteilung: Nur der Host (`isHost`, ermittelt über
 * `MatchSession.hostUid`) darf Runden weiterschalten ([startNextRound]) und
 * inaktive Spieler entfernen; alle anderen Aktionen (Tipp abgeben,
 * Heartbeat) führt jeder Client für sich selbst aus.
 */
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

    /** Interner veränderbarer StateFlow für den UI-Zustand des Multiplayer-Spiels. */
    private val _uiState = MutableStateFlow(GameUiState(isMultiplayer = true))

    /** Öffentlich beobachtbarer UI-Zustand für die Compose UI. */
    val uiState = _uiState.asStateFlow()

    /** Für alle Spieler identische Standorte der Session (siehe MatchSession.locationIds). */
    private var gameLocations: List<GeoLocation> = emptyList()

    /** Die eindeutige ID der aktuellen Multiplayer-Session. */
    private var sessionId: String = ""

    /** Lokal zwischengespeicherter Anzeigename, u. a. für Heartbeat-Updates ohne erneuten Profil-Zugriff. */
    private var playerName: String = "Spieler"

    /** Ob der lokale Nutzer aktuell der Host der Session ist (siehe MatchSession.hostUid). */
    private var isHost: Boolean = false

    /** Coroutine-Job für das periodische Senden des Presence-Heartbeats. */
    private var heartbeatJob: Job? = null

    /** Coroutine-Job für den rundenbasierten Sekundencountdown. */
    private var timerJob: Job? = null

    companion object {
        /** Intervall in Millisekunden, in dem das Lebenszeichen (Heartbeat) gesendet wird (10 Sek.). */
        private const val HEARTBEAT_INTERVAL_MS = 10_000L

        /** Timeout in Millisekunden, nach dem ein Mitspieler als offline/inaktiv gilt (40 Sek., toleriert Jitter/Doze). */
        private const val HEARTBEAT_TIMEOUT_MS = 40_000L
    }

    /**
     * Startet den Sekundentimer für die aktuelle Runde.
     * Zählt jede Sekunde herunter, sofern das Spiel nicht pausiert ist.
     * Nach Ablauf der Zeit wird die Runde automatisch ohne Tipp beendet ([finishRoundWithoutGuess]).
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && !_uiState.value.isRoundFinished && !_uiState.value.isGameFinished) {
                delay(1_000)
                val state = _uiState.value
                if (!state.isPaused && !state.isRoundFinished && !state.isGameFinished) {
                    _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
                }
            }

            // Automatischer Rundenabschluss bei abgelaufener Zeit
            if (_uiState.value.remainingSeconds == 0 && !_uiState.value.isRoundFinished && !_uiState.value.isGameFinished) {
                finishRoundWithoutGuess()
            }
        }
    }

    /**
     * Beendet die aktuelle Runde, falls der Spieler vor Ablauf der Zeit keinen Tipp abgegeben hat.
     * Vergibt 0 Punkte und 10.000 km Distanz als Strafe und zieht im Battle-Royale-Modus ein Leben ab.
     */
    private fun finishRoundWithoutGuess() {
        timerJob?.cancel()
        val state = _uiState.value
        val roundEntry = RoundStatistics(
            roundNumber = state.currentRound, score = 0, distanceKm = 10000.0
        )

        _uiState.update {
            it.copy(
                roundScore = 0,
                roundDistanceKilometers = 10000.0,
                isRoundFinished = true,
                waitingForPlayers = true,
                roundStatistics = it.roundStatistics + roundEntry
            )
        }

        viewModelScope.launch {
            val uid = firebaseAuthRepository.currentUid() ?: return@launch
            val currentPlayerState = state.multiplayerPlayers.find { it.uid == uid }
            var currentLives = currentPlayerState?.lives ?: 5
            if (state.gameMode == GameMode.BATTLE_ROYALE) {
                currentLives = (currentLives - 1).coerceAtLeast(0)
            }

            // Aktualisiert den Spielerzustand in Firebase
            sessionRepository.updatePlayerState(
                sessionId = sessionId,
                playerState = MultiplayerPlayerState(
                    uid = uid,
                    playerName = playerName,
                    score = state.totalScore,
                    round = state.currentRound,
                    lives = currentLives,
                    finishedRound = true,
                    currentGuessScore = 0,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Startet das Laden und Beobachten einer Session: aktiviert den
     * Heartbeat und abonniert sowohl den Session- als auch den
     * Spielerzustand parallel über zwei unabhängige Coroutines.
     */
    fun loadSessionLocations(sessionId: String) {
        this.sessionId = sessionId
        val currentUid = firebaseAuthRepository.currentUid() ?: return

        sessionRepository.registerSessionPresence(sessionId, currentUid)

        startHeartbeat()

        // Beobachtet Rundenwechsel, Spielmodus und Spielende auf Session-Ebene.
        viewModelScope.launch {
            sessionRepository.observeSession(sessionId).collect { session ->
                if (session == null) return@collect

                isHost = session.hostUid == currentUid

                // Session.mode wird als String gespeichert (siehe Lobby.mode-
                // Anmerkung); hier erfolgt die Umwandlung in das für GameUiState
                // benötigte GameMode-Enum, unabhängig von MultiplayerMode.
                val currentMode = when (session.mode) {
                    "BATTLE_ROYALE" -> GameMode.BATTLE_ROYALE
                    "PRO" -> GameMode.PRO
                    else -> GameMode.MULTIPLAYER
                }

                val myState = _uiState.value.multiplayerPlayers.find { it.uid == currentUid }
                val isEliminated = myState != null && myState.lives <= 0

                // Spielende entweder durch Session.finished (Host hat es
                // z. B. nach letzter Runde gesetzt) oder durch eigene
                // Elimination im Battle-Royale-Modus.
                if ((session.finished || isEliminated) && !_uiState.value.isGameFinished) {
                    _uiState.update { it.copy(isGameFinished = true, gameMode = currentMode) }
                    saveMatchResult()
                    return@collect
                }

                // Standorte werden nur einmal geladen, nicht bei jedem
                // Session-Update erneut (sonst unnötige wiederholte Abfragen).
                if (gameLocations.isEmpty()) {
                    val locations = getLocationsByIds(session.locationIds)
                    gameLocations = locations
                }

                val nextLocation = gameLocations.find { it.id == session.currentLocationId }

                val isUnlimitedRounds = currentMode == GameMode.BATTLE_ROYALE

                // Rundenwechsel wird erkannt, sobald sich Rundennummer oder
                // Standort-ID gegenüber dem aktuellen UI-State unterscheiden.
                if (session.currentRound != _uiState.value.currentRound || _uiState.value.currentLocation?.id != session.currentLocationId || (_uiState.value.isLoading && nextLocation != null)) {
                    _uiState.update {
                        it.copy(
                            currentRound = session.currentRound,
                            currentLocation = nextLocation,
                            totalRounds = session.totalRounds,
                            remainingSeconds = currentMode.roundDurationSeconds,
                            isRoundFinished = false,
                            guessedLocation = null,
                            roundScore = null,
                            roundDistanceKilometers = null,
                            viewMode = GameViewMode.STREET_VIEW,
                            isLoading = false,
                            gameMode = currentMode,
                            hasUnlimitedRounds = isUnlimitedRounds,
                            lives = myState?.lives ?: it.lives
                        )
                    }
                    startTimer()

                    // Der Host setzt den eigenen Spielerzustand bereits in
                    // startNextRound() zurück (finishedRound = false für alle
                    // Überlebenden). Nicht-Host-Clients müssen das hier separat
                    // für sich selbst nachholen, sobald sie den Rundenwechsel
                    // über die Session-Beobachtung bemerken.
                    if (!isHost) {
                        viewModelScope.launch {
                            val uid = firebaseAuthRepository.currentUid() ?: return@launch
                            val currentPlayer =
                                _uiState.value.multiplayerPlayers.find { it.uid == uid }

                            sessionRepository.updatePlayerState(
                                sessionId = sessionId, playerState = MultiplayerPlayerState(
                                    uid = uid,
                                    playerName = playerName,
                                    score = _uiState.value.totalScore,
                                    round = session.currentRound,
                                    lives = currentPlayer?.lives ?: 5,
                                    finishedRound = false,
                                    lastSeenTimestamp = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }
        }

        // Beobachtet die Spielerliste für Scoreboard, Auto-Win-Erkennung,
        // Inaktivitäts-Entfernung und den Host-gesteuerten Rundenwechsel.
        viewModelScope.launch {
            sessionRepository.observePlayerStates(sessionId).collect { players ->
                playerName = players.find { it.uid == currentUid }?.playerName ?: "Spieler"
                val myPlayer = players.find { it.uid == currentUid }

                _uiState.update {
                    it.copy(
                        multiplayerPlayers = players,
                        lives = myPlayer?.lives ?: it.lives
                    )
                }

                // Auto-Win: Greift erst ab Runde 2 (currentRound > 1), um jegliche
                // Race Conditions oder Fluktuationen beim Spielstart in Runde 1 auszuschließen.
                if (_uiState.value.currentRound > 1 && players.size == 1 && !_uiState.value.isGameFinished) {
                    _uiState.update { it.copy(isGameFinished = true) }
                    saveMatchResult()
                }

                // Nur der Host überwacht die Erreichbarkeit der übrigen
                // Spieler und entfernt sie bei Inaktivität (kein Heartbeat
                // seit mehr als 20 Sekunden, siehe startHeartbeat()).
                if (isHost) {
                    val now = System.currentTimeMillis()
                    val inactivePlayers = players.filter {
                        it.uid != currentUid && (now - it.lastSeenTimestamp > HEARTBEAT_TIMEOUT_MS)
                    }
                    inactivePlayers.forEach { inactive ->
                        viewModelScope.launch {
                            sessionRepository.removePlayerFromSession(sessionId, inactive.uid)
                        }
                    }
                }

                // Sobald alle Spieler ihren Tipp für die aktuelle Runde
                // abgegeben haben, schaltet ausschließlich der Host nach
                // kurzer Verzögerung zur nächsten Runde weiter, damit alle
                // Clients kurz das Rundenergebnis sehen können.
                val activePlayers = players.filter { it.lives > 0 }
                val allPlayersFinished = activePlayers.isNotEmpty() &&
                        activePlayers.all { it.finishedRound && it.round == _uiState.value.currentRound }

                if (isHost && allPlayersFinished && !_uiState.value.isGameFinished && _uiState.value.isRoundFinished) {
                    viewModelScope.launch {
                        delay(3000)
                        startNextRound()
                    }
                }
            }
        }
    }

    /**
     * Wählt die Koordinaten für den aktuellen Tipp des Spielers auf der Karte aus.
     *
     * @param guessedLocation Die vom Nutzer gewählte [GeoCoordinate].
     */
    fun selectGuess(guessedLocation: GeoCoordinate) {
        if (_uiState.value.isRoundFinished) return
        _uiState.update { it.copy(guessedLocation = guessedLocation) }
    }

    /** Schaltet die Ansicht auf die interaktive Ratekarte ([GameViewMode.GUESS_MAP]) um. */
    fun showGuessMap() {
        _uiState.update { it.copy(viewMode = GameViewMode.GUESS_MAP) }
    }

    /** Schaltet die Ansicht zurück auf das Street-View-Panorama ([GameViewMode.STREET_VIEW]). */
    fun showStreetView() {
        _uiState.update { it.copy(viewMode = GameViewMode.STREET_VIEW) }
    }

    /**
     * Verarbeitet den abgegebenen Tipp: berechnet Distanz und Punktzahl,
     * zieht im Battle-Royale-Modus bei Bedarf ein Leben ab und meldet den
     * neuen Zustand an Firebase, damit die anderen Spieler den Fortschritt
     * live sehen (siehe [MultiplayerScoreboard]).
     */
    fun submitGuess() {
        timerJob?.cancel()
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
            val roundEntry = RoundStatistics(
                roundNumber = state.currentRound, score = score, distanceKm = distance
            )

            // Battle-Royale-Regel (siehe Doku 5.1): Ein Leben wird
            // abgezogen, sobald die Schätzung mehr als 500 km vom
            // tatsächlichen Ort entfernt liegt.
            if (state.gameMode == GameMode.BATTLE_ROYALE && distance > 500.0) {
                currentLives = (currentLives - 1).coerceAtLeast(0)
            }

            _uiState.update {
                it.copy(
                    totalScore = updatedTotalScore,
                    roundScore = score,
                    roundDistanceKilometers = distance,
                    isRoundFinished = true,
                    waitingForPlayers = true,
                    roundStatistics = it.roundStatistics + roundEntry
                )
            }

            sessionRepository.updatePlayerState(
                sessionId = sessionId, playerState = MultiplayerPlayerState(
                    uid = uid,
                    playerName = playerName,
                    score = updatedTotalScore,
                    round = state.currentRound,
                    lives = currentLives,
                    finishedRound = true,
                    currentGuessScore = score,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
            )

            checkDailyQuests(distance, score, state.gameMode, currentLocation.region)
        }
    }

    /** Prüft nach jedem Tipp, ob dadurch eine Tagesquest erfüllt wurde. */
    private fun checkDailyQuests(distance: Double, score: Int, mode: GameMode, region: Region) {
        viewModelScope.launch {
            if (distance < 25.0) {
                val completed = dailyQuestRepository.updateQuestProgress("perfect_guess")
                if (completed != null) _uiState.update { it.copy(newlyCompletedQuest = completed) }
            }
            if (distance < 100.0 && region == Region.EUROPE) {
                val completed = dailyQuestRepository.updateQuestProgress("europe_explorer")
                if (completed != null) _uiState.update { it.copy(newlyCompletedQuest = completed) }
            }
            if (mode == GameMode.PRO) {
                val completed = dailyQuestRepository.updateQuestProgress("pro_mode_guess")
                if (completed != null) _uiState.update { it.copy(newlyCompletedQuest = completed) }
            }
        }
    }

    /** Blendet den Hinweisdialog für eine neu abgeschlossene Tagesquest aus. */
    fun dismissCompletedQuest() {
        _uiState.update { it.copy(newlyCompletedQuest = null) }
    }

    /**
     * Schaltet die Session zur nächsten Runde weiter oder beendet die
     * Partie, falls die letzte Runde erreicht ist oder (im Battle-Royale-
     * Modus) nur noch ein Spieler überlebt hat. Wirkt nur, wenn der lokale
     * Nutzer Host ist – alle anderen Clients reagieren stattdessen passiv
     * auf die Session-Beobachtung in [loadSessionLocations].
     */
    fun startNextRound() {
        if (!isHost) return

        val state = _uiState.value

        viewModelScope.launch {
            val survivors = state.multiplayerPlayers.filter { it.lives > 0 }
            val isLastRound =
                state.gameMode != GameMode.BATTLE_ROYALE && state.currentRound >= state.totalRounds
            val isBattleRoyaleDecided =
                state.gameMode == GameMode.BATTLE_ROYALE && survivors.size <= 1

            Log.d(
                "MULTIPLAYER_DEBUG",
                "currentRound=${state.currentRound}, " +
                        "totalRounds=${state.totalRounds}, " +
                        "players=${
                            state.multiplayerPlayers.map {
                                "${it.playerName}:lives=${it.lives}," +
                                        "round=${it.round},finished=${it.finishedRound}"
                            }
                        }"
            )


            val nextRound = state.currentRound + 1
            val nextLocation = gameLocations.getOrNull(nextRound - 1)
            val locationsExhausted = nextLocation == null

            if (isLastRound || isBattleRoyaleDecided || locationsExhausted) {
                sessionRepository.finishSession(sessionId)
                return@launch
            }

            val updatedPlayers = state.multiplayerPlayers.map { p ->
                if (p.lives > 0) {
                    p.copy(round = nextRound, finishedRound = false)
                } else {
                    p
                }
            }





            sessionRepository.advanceRoundAtomic(
                sessionId = sessionId,
                nextRound = nextRound,
                nextLocationId = nextLocation.id,
                updatedPlayers = updatedPlayers
            )
        }
    }

    /** Entfernt den lokalen Spieler aus der Session, z. B. beim vorzeitigen Verlassen der Partie. */
    fun leaveGame() {
        viewModelScope.launch {
            val currentUid = firebaseAuthRepository.currentUid() ?: return@launch
            sessionRepository.removePlayerFromSession(sessionId, currentUid)
        }
    }

    /**
     * Ermittelt Sieg/Niederlage über [determineWinner], speichert das
     * Matchergebnis in der Statistik und prüft anschließend
     * siegesabhängige Tagesquests. Stoppt zusätzlich den Heartbeat, da
     * nach Spielende keine weiteren Zustands-Updates mehr nötig sind.
     */
    private fun saveMatchResult() {
        timerJob?.cancel()
        heartbeatJob?.cancel()
        val state = _uiState.value
        val myUid = firebaseAuthRepository.currentUid() ?: return
        val (won, winnerName) = determineWinner(state, myUid)

        val match = MatchStatistic(
            timestamp = System.currentTimeMillis(),
            gameMode = state.gameMode.name,
            score = state.totalScore,
            rounds = state.currentRound,
            distanceKm = state.roundStatistics.sumOf { it.distanceKm },
            won = won,
            multiplayer = true
        )

        _uiState.update { it.copy(matchWinnerName = winnerName, isLocalPlayerWinner = won) }

        viewModelScope.launch {
            statisticsRepository.saveMatch(match)
            if (won) {
                val completed = dailyQuestRepository.updateQuestProgress("multiplayer_win")
                if (completed != null) _uiState.update { it.copy(newlyCompletedQuest = completed) }
                if (state.gameMode == GameMode.BATTLE_ROYALE) {
                    val brCompleted = dailyQuestRepository.updateQuestProgress("battle_royale_win")
                    if (brCompleted != null) _uiState.update { it.copy(newlyCompletedQuest = brCompleted) }
                }
            }
        }
    }

    /**
     * Bestimmt Sieger und Gewinnstatus des lokalen Spielers, abhängig vom
     * Spielmodus: im Battle-Royale-Modus gewinnt der letzte Überlebende,
     * ansonsten der Spieler mit der höchsten Gesamtpunktzahl.
     */
    private fun determineWinner(state: GameUiState, myUid: String): Pair<Boolean, String?> {
        return when (state.gameMode) {
            GameMode.BATTLE_ROYALE -> {
                val survivors = state.multiplayerPlayers.filter { it.lives > 0 }
                val topSurvivor = survivors.maxByOrNull { it.score }
                    ?: state.multiplayerPlayers.maxByOrNull { it.score }
                val won = topSurvivor?.uid == myUid
                won to topSurvivor?.playerName
            }

            else -> {
                val topPlayer = state.multiplayerPlayers.maxByOrNull { it.score }
                (topPlayer?.uid == myUid) to topPlayer?.playerName
            }
        }
    }

    /**
     * Sendet alle 10 Sekunden ein "Lebenszeichen" (siehe
     * [MultiplayerPlayerState.lastSeenTimestamp]) an Firebase, solange die
     * Partie läuft. Der Host nutzt diese Zeitstempel, um inaktive Spieler
     * zu erkennen und zu entfernen (siehe [loadSessionLocations]).
     *
     * Läuft als Endlosschleife im [viewModelScope], bis entweder kein Uid
     * mehr verfügbar ist (Abbruch der Schleife) oder [heartbeatJob] explizit
     * abgebrochen wird (Spielende via [saveMatchResult] oder [onCleared]).
     */
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
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }
    }

    /**
     * Räumt laufende Coroutine-Jobs (Timer, Heartbeat) auf,
     * wenn das ViewModel zerstört wird.
     */
    override fun onCleared() {
        timerJob?.cancel()
        heartbeatJob?.cancel()
    }
}