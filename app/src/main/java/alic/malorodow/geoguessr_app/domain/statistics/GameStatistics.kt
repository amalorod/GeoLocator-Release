package alic.malorodow.geoguessr_app.domain.statistics

import alic.malorodow.geoguessr_app.ui.components.MatchSummaryDialog

/**
 * Fasst das Ergebnis einer einzelnen, abgeschlossenen Partie zusammen.
 *
 * Dient als Eingabe für [MatchSummaryDialog] und wird nach Abschluss
 * aller Runden vom GameViewModel zusammengestellt (siehe Doku, Kapitel
 * 4.2 „Game Screen“).
 *
 * @property totalScore Über alle Runden aufsummierte Gesamtpunktzahl.
 * @property rounds Einzelergebnisse jeder gespielten Runde, dient u. a.
 *   der Ermittlung der besten Runde im Zusammenfassungsdialog.
 */
data class GameStatistics(
    val totalScore: Int,
    val rounds: List<RoundStatistics>
)