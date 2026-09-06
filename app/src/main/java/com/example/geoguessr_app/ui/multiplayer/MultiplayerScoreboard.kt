package com.example.geoguessr_app.ui.multiplayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.data.firebase.MultiplayerRepository
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerPlayerState
import com.example.geoguessr_app.ui.game.GameMode

/**
 * Zeigt den Live-Punktestand aller Mitspieler einer Multiplayer-Partie an.
 *
 * Passt sich der Bildschirmbreite an: Auf Tablets (> 600dp, siehe
 * isTablet) werden bis zu vier Spieler nebeneinander in einer [Row]
 * dargestellt, wobei sich die verfügbare Breite gleichmäßig auf alle
 * vorhandenen Spieler aufteilt (2 Spieler = 2 Kacheln zu je 50%, 4 Spieler
 * = 4 Kacheln zu je 25%). Auf Smartphones werden die Spieler stattdessen
 * untereinander mit "VS"-Trennern dazwischen dargestellt.
 *
 * Die Spieleranzahl ist auf maximal 4 begrenzt (siehe [MultiplayerRepository]/
 * Lobby-Beitritt), damit auch bei voller Besetzung die Kartenansicht im
 * Spiel bedienbar bleibt ([List.take] als zusätzliche Absicherung).
 *
 * @param players Aktueller Spielstand aller Sitzungsteilnehmer.
 * @param gameMode Aktiver Spielmodus; steuert, ob Herzen (Battle Royale)
 *   angezeigt werden.
 * @param localUid UID des lokalen Spielers, zur optischen Hervorhebung.
 */
@Composable
fun MultiplayerScoreboard(
    players: List<MultiplayerPlayerState>,
    gameMode: GameMode = GameMode.NORMAL,
    localUid: String = "",
    modifier: Modifier = Modifier
) {
    val displayPlayers = players.sortedByDescending { it.score }.take(MAX_PLAYERS)
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidthDp > 600

    if (isTablet) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            displayPlayers.forEach { player ->
                PlayerScoreCard(
                    player = player,
                    gameMode = gameMode,
                    isLocalPlayer = localUid.isNotEmpty() && player.uid == localUid,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            displayPlayers.forEachIndexed { index, player ->
                PlayerScoreCard(
                    player = player,
                    gameMode = gameMode,
                    isLocalPlayer = localUid.isNotEmpty() && player.uid == localUid,
                    modifier = Modifier.fillMaxWidth()
                )

                if (index < displayPlayers.lastIndex) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerScoreCard(
    player: MultiplayerPlayerState,
    gameMode: GameMode,
    isLocalPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isLocalPlayer) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isLocalPlayer) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = player.playerName,
                fontWeight = if (isLocalPlayer) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (gameMode == GameMode.BATTLE_ROYALE) {
                    Text("❤️ ${player.lives}")
                }
                Text(
                    text = "${player.score} Pkt.",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private const val MAX_PLAYERS = 4