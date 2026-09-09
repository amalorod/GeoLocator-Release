package alic.malorodow.geoguessr_app.domain.model.multiplayer

/**
 * Definiert die verfügbaren Multiplayer-Spielmodi (siehe Doku, Kapitel
 * 3.3: „Die Anwendung unterstützt mehrere Multiplayer-Modi, darunter
 * den normalen Modus sowie einen Battle-Royale-Modus“).
 *
 * ARCHITEKTUR-HINWEIS: Obwohl dieses Enum existiert, werden [Lobby.mode]
 * und [MatchSession.mode] als String statt als MultiplayerMode
 * gespeichert. Eine Umwandlung (z. B. über MultiplayerMode.valueOf(...)
 * bzw. .name) müsste demnach an den Stellen erfolgen, an denen der
 * Modus tatsächlich ausgewertet wird (z. B. LobbyViewModel).
 *
 * @property displayName Für Nutzer lesbarer, deutscher Anzeigename
 *   (z. B. in der Moduswahl innerhalb der Lobby).
 */
enum class MultiplayerMode(
    val displayName: String
) {
    FREEPLAY("Freies Spiel"),
    PRO("Pro"),
    BATTLE_ROYALE("Battle Royale")
}