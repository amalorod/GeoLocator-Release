package com.example.geoguessr_app.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.domain.model.statistics.LifetimeStatistics

@Composable
fun LifetimeStatisticsScreen(
    statistics: LifetimeStatistics,
    onBackClick: () -> Unit
) {

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
            text = "Karrierestatistik",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(24.dp))

        StatisticCard(
            title = "Partien gespielt",
            value = statistics.gamesPlayed.toString()
        )

        StatisticCard(
            title = "Runden gespielt",
            value = statistics.roundsPlayed.toString()
        )

        StatisticCard(
            title = "Gesamtpunkte",
            value = statistics.totalScore.toString()
        )

        StatisticCard(
            title = "Bestes Spiel",
            value = statistics.bestGameScore.toString()
        )

        StatisticCard(
            title = "Gesamtdistanz",
            value = "${statistics.totalDistanceKm.toInt()} km"
        )
    }
}

@Composable
private fun StatisticCard(
    title: String,
    value: String
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                style =
                    MaterialTheme.typography.labelLarge
            )

            Text(
                text = value,
                style =
                    MaterialTheme.typography.headlineSmall
            )
        }
    }
}