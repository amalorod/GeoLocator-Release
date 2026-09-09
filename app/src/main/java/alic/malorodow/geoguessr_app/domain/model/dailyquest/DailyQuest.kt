package alic.malorodow.geoguessr_app.domain.model.dailyquest

/**
 * Repräsentiert eine einzelne tägliche Herausforderung (siehe Doku,
 * Kapitel 4.6 „Daily Quest Screen“ und 5.2 „Daily Quest“).
 *
 * Sämtliche Properties besitzen Standardwerte, damit die Klasse ohne
 * Argumente instanziierbar ist. Das ist notwendig, da Firebase
 * Realtime Database beim Deserialisieren von Datenbank-Snapshots einen
 * parameterlosen Konstruktor benötigt (siehe DailyQuestRepository).
 *
 * @property id Eindeutiger Bezeichner der Quest, dient als Schlüssel
 *   beim Speichern des Fortschritts in Firebase.
 * @property title Kurzer Anzeigetitel der Aufgabe.
 * @property description Ausführlichere Beschreibung, was zu tun ist
 *   (z. B. „Erziele 3 Treffer innerhalb von 100 km“).
 * @property icon Bezeichner/Ressourcenname des angezeigten Icons.
 * @property completed Gibt an, ob die Quest bereits abgeschlossen wurde
 *   (steuert u. a. die grüne Haken-Markierung, siehe Doku 4.6).
 * @property progress Aktueller Fortschritt in Bezug auf [target] (z. B.
 *   1 von 3 erforderlichen Treffern).
 * @property target Zielwert, der erreicht werden muss, damit
 *   [completed] auf true gesetzt wird.
 */
data class DailyQuest(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val icon: String = "",
    val completed: Boolean = false,
    val progress: Int = 0,
    val target: Int = 1
)