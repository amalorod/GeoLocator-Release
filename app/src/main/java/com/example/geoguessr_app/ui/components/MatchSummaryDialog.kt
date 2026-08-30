package com.example.geoguessr_app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.domain.statistics.GameStatistics

/**
 * Zeigt nach Abschluss einer Partie eine Zusammenfassung der wichtigsten
 * Ergebnisse an (siehe Doku, Kapitel 4.2 „Game Screen").
 *
 * @param statistics Ergebnis der soeben beendeten Partie.
 * @param isMultiplayer Steuert, ob zusätzlich eine Sieger-Zeile angezeigt wird.
 * @param isLocalPlayerWinner Ob der aktuelle Nutzer die Partie gewonnen hat
 *   (nur relevant, wenn [isMultiplayer] true ist).
 * @param winnerName Anzeigename des Siegers, falls bekannt.
 * @param onDetailsClick Callback, um eine detailliertere Rundenübersicht zu öffnen.
 * @param onNewGameClick Callback, um direkt eine neue Partie zu starten.
 * @param onHomeClick Callback, um zum Home Screen zurückzukehren.
 */
@Composable
fun MatchSummaryDialog(
    statistics: GameStatistics,
    isMultiplayer: Boolean = false,
    isLocalPlayerWinner: Boolean? = null,
    winnerName: String? = null,
    onDetailsClick: () -> Unit,
    onNewGameClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val bestRound = statistics.rounds.maxByOrNull { it.score }
    val averageScore = if (statistics.rounds.isEmpty()) {
        0
    } else {
        statistics.totalScore / statistics.rounds.size
    }

    AlertDialog(onDismissRequest = {}, title = {
        Text(
            if (isMultiplayer && isLocalPlayerWinner == true) "🏆 Sieger!"
            else if (isMultiplayer) "Partie beendet"
            else "🏆 Partie beendet"
        )
    }, text = {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isMultiplayer) {
                val resultText = when {
                    isLocalPlayerWinner == true -> "Du hast gewonnen! 🎉"
                    winnerName != null -> "Sieger: $winnerName"
                    else -> "Partie beendet"
                }
                Text(text = resultText)
            }
            Text(text = "Gesamtpunkte: ${statistics.totalScore}")
            Text(text = "Beste Runde: ${bestRound?.score ?: 0}")
            Text(text = "Ø Punkte: $averageScore")
        }
    }, confirmButton = {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            TextButton(onClick = onDetailsClick) {
                Text("Details")
            }
            TextButton(onClick = onNewGameClick) {
                Text("Neues Spiel")
            }
            TextButton(onClick = onHomeClick) {
                Text("Home")
            }
        }
    })
}