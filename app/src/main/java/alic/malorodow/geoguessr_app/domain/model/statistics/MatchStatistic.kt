package alic.malorodow.geoguessr_app.domain.model.statistics

import alic.malorodow.geoguessr_app.domain.model.dailyquest.DailyQuest
import alic.malorodow.geoguessr_app.domain.model.profile.PlayerProfile

/**
 * Repräsentiert die Statistik einer einzelnen, abgeschlossenen Partie
 * (siehe Doku, Kapitel 4.4 „Statistics Screen“ und 5.6 „Profil-,
 * Statistik- und Cloud-System“).
 *
 * Wie [DailyQuest] besitzt jede Property einen Standardwert, um die
 * Deserialisierung aus Firebase Realtime Database über einen
 * parameterlosen Konstruktor zu ermöglichen. Da hier ausschließlich val
 * statt var verwendet wird, ist das Objekt nach der Erzeugung
 * unveränderlich – die Werte werden vom Firebase-Deserializer direkt
 * über den Konstruktor gesetzt statt nachträglich per Reflection, was
 * (anders als bei [PlayerProfile]) keine var-Properties erfordert.
 *
 * Eine Liste dieser Objekte bildet die Grundlage für die
 * Aktivitätsliste und den Score-Verlauf im Statistics Screen (siehe
 * Doku 4.4).
 *
 * @property timestamp Zeitpunkt des Spielendes in Millisekunden seit
 *   Unix-Epoch, dient der chronologischen Sortierung im Verlauf.
 * @property gameMode Bezeichner des gespielten Modus (z. B. „NORMAL“,
 *   „CUSTOM“, „MULTIPLAYER“) als String statt Enum, da Strings robuster
 *   gegen Schema-Änderungen sind und sich besser für die Speicherung
 *   in Firebase eignen
 * @property score Erzielte Gesamtpunktzahl der Partie.
 * @property rounds Anzahl gespielter Runden.
 * @property distanceKm Distanz, die der Spieler während der Partie
 * @property won Gibt an, ob die Partie gewonnen wurde – relevant
 *   insbesondere für Multiplayer- und Battle-Royale-Partien.
 * @property multiplayer Kennzeichnet, ob es sich um eine Mehrspieler-
 *   Partie handelte, zur Unterscheidung von Einzelspieler-Statistiken.
 */
data class MatchStatistic(
    val timestamp: Long = 0,
    val gameMode: String = "",
    val score: Int = 0,
    val rounds: Int = 0,
    val distanceKm: Double = 0.0,
    val won: Boolean = false,
    val multiplayer: Boolean = false
)