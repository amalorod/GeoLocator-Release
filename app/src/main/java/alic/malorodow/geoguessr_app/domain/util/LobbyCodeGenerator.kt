package alic.malorodow.geoguessr_app.domain.util

import alic.malorodow.geoguessr_app.domain.util.LobbyCodeGenerator.CHARSET
import kotlin.random.Random

/**
 * Erzeugt zufällige, für Nutzer gut lesbare Lobby-Codes zum Beitreten
 * einer Multiplayer-Lobby (siehe Doku, Kapitel 3.1 „Lobbys erstellen“).
 *
 * Als object statt als Klasse modelliert, da der Generator keinen
 * eigenen Zustand besitzt und global im gesamten Projekt unter
 * demselben Namen aufrufbar sein soll (echtes Kotlin-Singleton).
 *
 * Der verwendete CHARSET enthält bewusst nicht das vollständige
 * Alphabet und alle Ziffern: Verwechslungsanfällige Zeichen wie I/1,
 * O/0 oder L wurden entfernt, um Tippfehler beim manuellen Weitergeben
 * eines Codes zu vermeiden.
 */
object LobbyCodeGenerator {

    private const val CHARSET =
        "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    /**
     * Erzeugt einen sechsstelligen Lobby-Code durch sechsfaches
     * zufälliges Ziehen eines Zeichens aus [CHARSET].
     */
    fun generate(): String {
        return buildString {
            repeat(6) {
                append(CHARSET[Random.nextInt(CHARSET.length)])
            }
        }
    }
}