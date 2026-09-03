package com.example.geoguessr_app.ui.multiplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState

/**
 * Zeigt den Live-Punktestand aller Mitspieler einer Multiplayer-Partie an.
 *
 * Passt sich der Bildschirmbreite an: Auf Tablets (> 600dp, siehe
 * [isTablet]) werden bis zu vier Spieler nebeneinander in einer [Row]
 * dargestellt, auf Smartphones bis zu drei Spieler untereinander mit
 * "VS"-Trennern dazwischen – mehr als drei/vier Spieler werden aus
 * Platzgründen abgeschnitten ([List.take]).
 *
 * @param players Aktueller Spielstand aller Sitzungsteilnehmer.
 */
@Composable
fun MultiplayerScoreboard(
    players: List<MultiplayerPlayerState>,
    totalRounds: Int,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    if (isTablet) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            players.take(4).forEachIndexed { index, player ->
                MultiplayerPlayerCard(
                    player = player,
                    totalRounds = totalRounds,
                    color = when (index) {
                        0 -> MaterialTheme.colorScheme.primary
                        1 -> Color(0xFF1565C0)
                        2 -> Color(0xFF2E7D32)
                        else -> Color(0xFF7B1FA2)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            players.take(3).forEachIndexed { index, player ->
                MultiplayerPlayerCard(
                    player = player,
                    totalRounds = totalRounds,
                    color = when (index) {
                        0 -> MaterialTheme.colorScheme.primary
                        1 -> Color(0xFF1565C0)
                        else -> Color(0xFF2E7D32)
                    }
                )

                if (index < players.lastIndex && index < 2) {
                    Text(
                        text = "VS",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/** Einzelne Spielerkarte mit Name, Rundenstand, Leben (falls relevant) und Score. */
@Composable
private fun MultiplayerPlayerCard(
    player: MultiplayerPlayerState,
    totalRounds: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color,
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = player.playerName,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Runde ${player.round}/ $totalRounds",
                    style = MaterialTheme.typography.labelSmall
                )

                if (player.lives > 0) {
                    Text(
                        text = "❤️".repeat(player.lives),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        text = "ELIMINIERT 💀",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = player.score.toString(),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}