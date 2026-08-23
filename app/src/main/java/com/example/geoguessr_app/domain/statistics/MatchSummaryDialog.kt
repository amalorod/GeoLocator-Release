package com.example.geoguessr_app.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.domain.model.statistics.GameStatistics

@Composable
fun MatchSummaryDialog(
    statistics: GameStatistics,
    onDetailsClick: () -> Unit,
    onNewGameClick: () -> Unit,
    onHomeClick: () -> Unit
) {

    val bestRound =
        statistics.rounds.maxByOrNull { it.score }

    val averageScore =
        if (statistics.rounds.isEmpty()) {
            0
        } else {
            statistics.totalScore /
                    statistics.rounds.size
        }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text("🏆 Partie beendet")
        },
        text = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text =
                        "Gesamtpunkte: ${statistics.totalScore}"
                )

                Text(
                    text =
                        "Beste Runde: ${bestRound?.score ?: 0}"
                )

                Text(
                    text =
                        "Ø Punkte: $averageScore"
                )
            }
        },
        confirmButton = {

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {

                TextButton(
                    onClick = onDetailsClick
                ) {
                    Text("Details")
                }

                TextButton(
                    onClick = onNewGameClick
                ) {
                    Text("Neues Spiel")
                }

                TextButton(
                    onClick = onHomeClick
                ) {
                    Text("Home")
                }
            }
        }
    )
}