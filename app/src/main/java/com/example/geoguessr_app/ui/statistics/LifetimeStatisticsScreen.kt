package com.example.geoguessr_app.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geoguessr_app.data.statistics.StatisticsRepository
import com.example.geoguessr_app.domain.statistics.LifetimeStatistics
import com.example.geoguessr_app.domain.model.statistics.MatchStatistic

@Composable
fun LifetimeStatisticsScreen(
    statistics: LifetimeStatistics,
    onBackClick: () -> Unit
) {
    val recentMatches by StatisticsRepository.recentMatches.collectAsState()
    val topPlayers by StatisticsRepository.topPlayers.collectAsState()
    val scrollState = rememberScrollState()
    
    var showLeaderboard by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Einheitlicher Header (bombenfest)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 12.dp),
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
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                        .padding(20.dp)
                ) {
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
                MiniCard(title = "Spiele", value = statistics.gamesPlayed.toString(), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                MiniCard(title = "Punkte", value = statistics.totalScore.toString(), modifier = Modifier.weight(1.5f))
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                MiniCard(title = "Ø Score", value = if (statistics.gamesPlayed > 0) (statistics.totalScore / statistics.gamesPlayed).toString() else "0", modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                MiniCard(title = "Beste", value = statistics.bestGameScore.toString(), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showLeaderboard = true },
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
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    if (topPlayers.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text("Lade Top-Spieler...", modifier = Modifier.align(Alignment.CenterHorizontally))
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

@Composable
private fun LeaderboardItem(rank: Int, entry: com.example.geoguessr_app.data.statistics.LeaderboardEntry) {
    val medal = when(rank) {
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
                val avgScore = if (entry.stats.gamesPlayed > 0) entry.stats.totalScore / entry.stats.gamesPlayed else 0
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

@Composable
private fun ScoreChart(scores: List<Int>, modifier: Modifier = Modifier) {
    if (scores.isEmpty()) return
    
    val maxScore = scores.maxOrNull()?.coerceAtLeast(1) ?: 1
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
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

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx())
        )
        
        points.forEach { point ->
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun MiniCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

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
                Text("SIEG 🏆", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
            }
        }
    }
}
