package com.example.geoguessr_app.ui.multiplayer

import com.example.geoguessr_app.domain.model.multiplayer.LobbyPlayer
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerMode
import com.example.geoguessr_app.navigation.GeoGuessrNavHost

/**
 * Zustand der Lobby-Phase vor Spielbeginn: Warteraum, in dem Spielende
 * warten bis alle den Bereit-Button gedrückt haben und der Host den Spielmodus festlegt.
 *
 * Sobald [started] auf true wechselt und [sessionId] befüllt ist,
 * navigiert [GeoGuessrNavHost] automatisch zum eigentlichen
 * Multiplayer-Spielbildschirm (siehe LaunchedEffect in der NavHost-Route
 * für MultiplayerLobby).
 *
 * @property lobbyCode Für Mitspieler sichtbarer Beitritts-Code dieser Lobby.
 * @property isLoading Ob aktuell eine Lobby-Operation (Erstellen/Beitreten) läuft.
 * @property currentUserUid UID des lokalen Nutzers; dient dem Abgleich, welcher
 * Eintrag in [players] "man selbst" ist (z. B. für Ready-Button-Zustand).
 * @property players Aktueller Teilnehmerstatus aller Lobby-Mitglieder.
 * @property selectedMode Vom Host gewählter Spielmodus für diese Partie.
 * @property started Ob der Host die Partie bereits gestartet hat.
 * @property sessionId ID der erzeugten Spiel-Session; erst gültig, wenn [started] true ist.
 * @property errorMessage Fehlermeldung bei fehlgeschlagenem Beitritt/Erstellen; null im Normalfall.
 */
data class LobbyUiState(
    val lobbyCode: String = "",
    val isLoading: Boolean = false,
    val currentUserUid: String = "",
    val players: List<LobbyPlayer> = emptyList(),
    val selectedMode: MultiplayerMode = MultiplayerMode.FREEPLAY,
    val started: Boolean = false,
    val sessionId: String = "",
    val errorMessage: String? = null
)