package com.example.geoguessr_app.ui.game

import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.model.dailyquest.DailyQuest
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState
import com.example.geoguessr_app.domain.statistics.GameStatistics
import com.example.geoguessr_app.domain.statistics.RoundStatistics

/**
 * Beschreibt den vollständigen sichtbaren Zustand des Spiels.
 *
 * Das ViewModel erzeugt bei jeder Änderung mit copy() eine neue Instanz.
 * Dadurch bleibt der Zustand unveränderlich (immutable) und nachvollziehbar
 * – ein zentrales Prinzip von unidirektionalem State Management: Die UI
 * kann [GameUiState] niemals selbst mutieren, sondern erhält bei jeder
 * Änderung ausschließlich eine neue, vollständige Momentaufnahme.
 *
 * Die Felder decken sowohl den Einzelspieler- als auch den Multiplayer-
 * Fall ab (siehe [isMultiplayer], [sessionId], [multiplayerPlayers],
 * [matchWinnerName]); das vermeidet zwei parallele, fast identische
 * UiState-Klassen und hält [GameScreen] als einzige Konsument-Composable
 * für beide Spielarten wiederverwendbar.
 *
 * @property currentRound Nummer der aktuell laufenden Runde (1-basiert).
 * @property totalRounds Gesamtzahl der Runden dieser Partie.
 * @property remainingSeconds Verbleibende Zeit der aktuellen Runde in Sekunden.
 * @property totalScore Aufsummierte Punktzahl über alle bisherigen Runden.
 * @property currentLocation Standort der aktuellen Runde; null während des Ladens.
 * @property guessedLocation Vom Spielenden auf der Karte gewählter Tipp-Standort.
 * @property roundDistanceKilometers Entfernung zwischen Tipp und tatsächlichem
 * Standort der zuletzt abgeschlossenen Runde.
 * @property roundScore Erzielte Punktzahl der zuletzt abgeschlossenen Runde.
 * @property viewMode Aktuell sichtbarer Bereich (Street View oder Weltkarte).
 * @property isPaused Ob die Partie aktuell pausiert ist.
 * @property isLoading Ob die Standorte der Partie noch geladen werden.
 * @property isRoundFinished Ob die aktuelle Runde abgeschlossen ist (Ergebnis-Anzeige).
 * @property isGameFinished Ob die gesamte Partie abgeschlossen ist (Endergebnis-Anzeige).
 * @property errorMessage Fehlermeldung beim Laden der Standorte; null, wenn kein Fehler vorliegt.
 * @property gameMode Der für diese Partie aktive [GameMode].
 * @property roundStatistics Statistik-Datensätze aller bisher abgeschlossenen Runden.
 * @property gameStatistics Zusammenfassende Statistik der gesamten Partie; wird erst bei [isGameFinished] befüllt.
 * @property isMultiplayer Ob es sich um eine Mehrspieler-Partie handelt.
 * @property sessionId ID der Multiplayer-Session; null im Einzelspielermodus.
 * @property waitingForPlayers Ob aktuell auf andere Mitspieler gewartet wird (Multiplayer).
 * @property multiplayerPlayers Aktueller Status aller Mitspieler (Multiplayer).
 * @property newlyCompletedQuest Gerade erst erfüllte Daily Quest, für die ein
 * Erfolgs-Dialog angezeigt werden soll; null, wenn keine Quest aktuell zu feiern ist.
 * @property lives Verbleibende Leben im Custom- bzw. Battle-Royale-Modus;
 * [Int.MAX_VALUE] als Default signalisiert "unbegrenzt/nicht relevant" für Modi ohne Lebenssystem.
 * @property isStreetViewNavigationEnabled Ob im Street View frei navigiert werden darf
 * (abgeleitet aus [GameMode.streetViewNavigationEnabled] des aktiven Modus).
 * @property matchWinnerName Name des Gewinners einer Multiplayer-Partie; null im Einzelspielermodus.
 * @property isLocalPlayerWinner Ob der lokale Spielende die Multiplayer-Partie gewonnen hat.
 * @property hasUnlimitedRounds Ob für den aktuellen Spielmodus kein festes Rundenlimit gilt
 * (z. B. Battle Royale oder Custom Mittel/Schwer).
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
    val multiplayerPlayers: List<MultiplayerPlayerState> = emptyList(),
    val newlyCompletedQuest: DailyQuest? = null,
    val lives: Int = Int.MAX_VALUE,
    val isStreetViewNavigationEnabled: Boolean = true,
    val matchWinnerName: String? = null,
    val isLocalPlayerWinner: Boolean? = null,
    val hasUnlimitedRounds: Boolean = false
)