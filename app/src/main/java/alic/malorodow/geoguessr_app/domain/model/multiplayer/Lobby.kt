package alic.malorodow.geoguessr_app.domain.model.multiplayer

import com.google.firebase.database.PropertyName

/**
 * Repräsentiert eine Multiplayer-Lobby vor Spielbeginn (siehe Doku,
 * Kapitel 3.1 „Lobbys erstellen“ und 3.2 „Lobby beitreten“).
 *
 * Alle Properties besitzen Standardwerte, damit ein parameterloser
 * Konstruktor für die Deserialisierung aus Firebase Realtime Database
 * existiert (siehe Erläuterung in PlayerProfile).
 *
 * @property lobbyCode Eindeutiger, an andere Spieler weitergebbarer
 *   Code zum Beitreten der Lobby (siehe Doku 3.1).
 * @property hostUid Firebase-UID des aktuellen Hosts. Verlässt der Host
 *   die Lobby, wird laut Doku (3.2) automatisch ein neuer Host
 *   bestimmt – dieses Feld wird entsprechend aktualisiert.
 * @property mode Ausgewählter Multiplayer-Modus als String (siehe
 *   [MultiplayerMode]). ARCHITEKTUR-HINWEIS: Hier wird bewusst ein
 *   String statt des Enums direkt verwendet, vermutlich zur
 *   einfacheren Firebase-Serialisierung; die Umwandlung in/aus
 *   MultiplayerMode müsste an der Verwendungsstelle erfolgen.
 * @property players Map für alle aktuell in der Lobby befindlichen
 *   Spieler (siehe [LobbyPlayer]).
 * @property started Kennzeichnet, ob der Host das Spiel bereits
 *   gestartet hat. Explizit mit @PropertyName annotiert (im Gegensatz
 *   zu den übrigen Feldern), vermutlich um Konsistenz mit einem
 *   bestehenden Firebase-Schema sicherzustellen; als var deklariert,
 *   da dieses Feld nach Lobby-Erstellung serverseitig verändert wird.
 * @property sessionId Verweist auf die zugehörige [MatchSession],
 *   sobald das Spiel gestartet wurde (siehe GeoGuessrNavHost, in dem
 *   anhand von started und sessionId zur Multiplayer-Partie navigiert
 *   wird).
 */
data class Lobby(
    val lobbyCode: String = "",
    val hostUid: String = "",
    val mode: String = "FREEPLAY",
    val players: Map<String, LobbyPlayer> = emptyMap(),
    @get:PropertyName("started")
    @set:PropertyName("started")
    var started: Boolean = false,
    val sessionId: String = ""
)