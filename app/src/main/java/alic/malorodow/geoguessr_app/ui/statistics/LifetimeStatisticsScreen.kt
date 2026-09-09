package alic.malorodow.geoguessr_app.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import alic.malorodow.geoguessr_app.data.statistics.LeaderboardEntry
import alic.malorodow.geoguessr_app.domain.model.statistics.MatchStatistic
import alic.malorodow.geoguessr_app.domain.statistics.LifetimeStatistics
import alic.malorodow.geoguessr_app.navigation.GeoGuessrNavHost

/**
 * Zeigt Lebenszeit-Statistiken, einen Score-Verlauf der letzten Partien
 * sowie ein globales Leaderboard (als Dialog) an.
 *
 * @param statistics Aggregierte Lebenszeit-Werte, von außen übergeben (siehe
 * [GeoGuessrNavHost], das [StatisticsViewModel.statistics] bereits dort sammelt).
 * @param isGuest Ob der aktuelle Nutzer ohne Profil spielt; steuert, ob im
 * Leaderboard-Dialog echte Daten oder ein Login-Hinweis angezeigt werden.
 * @param onBackClick Navigiert zurück zum vorherigen Bildschirm.
 * @param viewModel Verwaltet zusätzlich [StatisticsViewModel.recentMatches] und [
 * StatisticsViewModel.topPlayers], die hier direkt beobachtet werden (im Gegensatz zu [statistics],
 * das bereits von außen hereingereicht wird).
 */
@Composable
fun LifetimeStatisticsScreen(
    statistics: LifetimeStatistics,
    isGuest: Boolean,
    onBackClick: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val recentMatches by viewModel.recentMatches.collectAsState()
    val topPlayers by viewModel.topPlayers.collectAsState()
    val scrollState = rememberScrollState()

    var showLeaderboard by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBackClick) {
                    Text(" Zurück ", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Statistik",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Der Score-Chart wird nur angezeigt, wenn bereits Partien
            // vorhanden sind
            if (recentMatches.isNotEmpty()) {
                Text(
                    text = "Score-Verlauf (Letzte 20)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.shapes.medium
                        )
                        .padding(20.dp)
                ) {
                    // recentMatches liegt neueste-zuerst vor (siehe
                    // StatisticsRepository). Für den Chart wird die
                    // Reihenfolge umgedreht, damit die Zeitachse von
                    // links (ältestes Spiel) nach rechts (neuestes Spiel) verläuft.
                    ScoreChart(
                        scores = recentMatches.map { it.score }.reversed(),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Text(
                text = "Zusammenfassung",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                MiniCard(
                    title = "Spiele",
                    value = statistics.gamesPlayed.toString(),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                MiniCard(
                    title = "Punkte",
                    value = statistics.totalScore.toString(),
                    modifier = Modifier.weight(1.5f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                MiniCard(
                    title = "Ø Score",
                    value = averageScore(statistics.totalScore, statistics.gamesPlayed).toString(),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                MiniCard(
                    title = "Beste",
                    value = statistics.bestGameScore.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    showLeaderboard = true
                    // Lädt das Leaderboard nur für eingeloggte Nutzer nach.
                    // Gäste sehen im Dialog nur den Login-Hinweis,
                    // ein Request wäre für sie unnötig. loadLeaderboard()
                    // selbst verhindert zusätzlich wiederholte Requests,
                    // falls bereits Daten vorliegen.
                    if (!isGuest) viewModel.loadLeaderboard()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("🏆 Leaderboard 🏆", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Letzte Spiele",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            recentMatches.forEach { match ->
                MatchEntry(match)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            if (recentMatches.isEmpty()) {
                Text(
                    text = "Noch keine Daten vorhanden.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showLeaderboard) {
        AlertDialog(
            onDismissRequest = { showLeaderboard = false },
            title = {
                Text(
                    "Globales Leaderboard",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    if (isGuest) {
                        Text(
                            "Um das Leaderboard einzusehen, bitte einloggen.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    } else if (topPlayers.isEmpty()) {
                        // Zeigt den Ladezustand, solange loadLeaderboard()
                        // noch keine Daten zurückgeliefert hat.
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text(
                            "Lade Top-Spieler...",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        topPlayers.forEachIndexed { index, entry ->
                            LeaderboardItem(index + 1, entry)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLeaderboard = false }) {
                    Text("Schließen", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/** Berechnet den Durchschnitts-Score sicher gegen Division durch Null. */
private fun averageScore(totalScore: Int, gamesPlayed: Int): Int {
    return if (gamesPlayed > 0) totalScore / gamesPlayed else 0
}

/** Einzelner Leaderboard-Eintrag mit Rang (Medaille für Top 3), Name und Durchschnittspunkten. */
@Composable
private fun LeaderboardItem(rank: Int, entry: LeaderboardEntry) {
    val medal = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$rank"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = medal, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.playerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${entry.stats.gamesPlayed} Spiele · Bestes Spiel: ${entry.stats.bestGameScore}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val avgScore = averageScore(entry.stats.totalScore, entry.stats.gamesPlayed)
                Text(
                    text = "$avgScore",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Ø Punkte",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/**
 * Zeichnet einen einfachen Liniendiagramm-Verlauf der übergebenen Scores
 * per [Canvas]. Bewusst als eigenes, leichtgewichtiges Zeichnen statt einer
 * externen Chart-Bibliothek umgesetzt, da nur eine simple Linie mit Punkten
 * benötigt wird.
 */
@Composable
private fun ScoreChart(scores: List<Int>, modifier: Modifier = Modifier) {
    if (scores.isEmpty()) return

    val maxScore = scores.maxOrNull()?.coerceAtLeast(1) ?: 1
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        // Bei nur einem Datenpunkt gibt es keinen Abstand zu berechnen;
        // spacing = width verhindert hier eine Division durch Null.
        val spacing = if (scores.size > 1) width / (scores.size - 1) else width

        val points = scores.mapIndexed { index, score ->
            Offset(
                x = index * spacing,
                y = height - (score.toFloat() / maxScore * height)
            )
        }

        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
        }

        drawPath(path = path, color = primaryColor, style = Stroke(width = 3.dp.toPx()))

        points.forEach { point ->
            drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = point)
        }
    }
}

/** Kompakte Kachel zur Anzeige eines einzelnen Statistik-Werts (z. B. "Spiele: 12"). */
@Composable
private fun MiniCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Einzelner Eintrag in der Liste der letzten Partien: Spielart, Modus,
 * erzielte Punktzahl und – bei Multiplayer-Siegen – eine Sieg-Markierung.
 *
 * Verwendet dieselbe feste Grün-Farbe (0xFF2E7D32) wie an anderen Stellen
 * der App (z. B. MultiplayerLobbyScreen für "BEREIT") als konsistente Farbe,
 * unabhängig vom aktiven Theme.
 */
@Composable
private fun MatchEntry(match: MatchStatistic) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (match.multiplayer) "Multiplayer" else "Singleplayer",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = match.gameMode,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${match.score} Pkt.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (match.won) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
            )
            if (match.multiplayer && match.won) {
                Text(
                    "SIEG 🏆",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}