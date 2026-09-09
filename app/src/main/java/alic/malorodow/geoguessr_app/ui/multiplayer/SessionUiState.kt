package alic.malorodow.geoguessr_app.ui.multiplayer

import alic.malorodow.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState

/**
 * Zustand der laufenden Multiplayer-Partie nach Spielbeginn (im Gegensatz
 * zu [LobbyUiState], das die vorherige Warteraum-Phase beschreibt).
 *
 * Deutlich schlanker als [LobbyUiState], da während der laufenden Partie
 * primär [MultiplayerGameViewModel] den Spielablauf steuert (Runden,
 * Standorte, eigene Tipps) – dieser State bildet ausschließlich den
 * Synchronisationsstatus mit den anderen Mitspielern ab.
 *
 * @property sessionId ID der aktuell laufenden Session (übernommen aus
 * [LobbyUiState.sessionId] beim Übergang von Lobby zu Spiel).
 * @property players Aktueller Fortschritt/Status aller Mitspieler dieser Session.
 * @property allPlayersFinished Ob alle Mitspieler die aktuelle Runde/Partie
 * abgeschlossen haben; steuert vermutlich den Übergang zur nächsten Runde
 * oder zum Endergebnis-Dialog.
 */
data class SessionUiState(
    val sessionId: String = "",
    val players: List<MultiplayerPlayerState> = emptyList(),
    val allPlayersFinished: Boolean = false
)