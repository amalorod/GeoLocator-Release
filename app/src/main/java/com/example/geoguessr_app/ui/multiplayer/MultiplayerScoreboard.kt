package com.example.geoguessr_app.ui.multiplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState

@Composable
fun MultiplayerScoreboard(
    players: List<MultiplayerPlayerState>,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        players.take(3).forEachIndexed { index, player ->

            MultiplayerPlayerCard(
                player = player,
                color = when (index) {

                    0 -> MaterialTheme.colorScheme.primary

                    1 -> Color(0xFF1565C0)

                    else -> Color(0xFF2E7D32)
                }
            )

            if (index < players.lastIndex) {

                Text(
                    text = "VS",
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MultiplayerPlayerCard(
    player: MultiplayerPlayerState,
    color: Color
) {

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color,
        contentColor = Color.White
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = player.playerName,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text =
                        "Runde ${player.round}/5"
                )
            }

            Text(
                text = player.score.toString(),
                style =
                    MaterialTheme.typography
                        .titleLarge
            )
        }
    }
}