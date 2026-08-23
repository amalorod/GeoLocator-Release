package com.example.geoguessr_app.ui.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.domain.model.statistics.RoundStatistics


@Composable
fun StatisticsScreen(
    totalScore: Int,
    statistics: List<RoundStatistics>,
    onBackClick: () -> Unit
) {
    val bestRound =
        statistics.maxByOrNull { it.score }

    val averageScore =
        if (statistics.isEmpty()) {
            0
        } else {
            totalScore / statistics.size
        }

    val averageDistance =
        if (statistics.isEmpty()) {
            0.0
        } else {
            statistics
                .map { it.distanceKm }
                .average()
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Button(
            onClick = onBackClick
        ) {
            Text("Zurück")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Spielstatistik",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gesamtpunkte: $totalScore",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text =
                "Beste Runde: ${bestRound?.score ?: 0}"
        )

        Text(
            text =
                "Ø Punkte: $averageScore"
        )

        Text(
            text =
                "Ø Distanz: ${averageDistance.toInt()} km"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Rundenübersicht",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        statistics.forEach { round ->

            Text(
                text =
                    "Runde ${round.roundNumber} • ${round.score} Punkte",
                style = MaterialTheme.typography.titleSmall
            )

            LinearProgressIndicator(
                progress = {
                    (round.score / 5000f)
                        .coerceIn(0f, 1f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor =
                    MaterialTheme.colorScheme.primaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text =
                    "Distanz: ${
                        round.distanceKm.toInt()
                    } km"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}