package alic.malorodow.geoguessr_app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import alic.malorodow.geoguessr_app.ui.game.GameMode

/**
 * Hilfsklasse für Icons und Beschreibungen der Spielmodi.
 */
data class GameModeInfo(
    val icon: String,
    val description: String
)

fun getGameModeInfo(mode: GameMode): GameModeInfo {
    return when (mode) {
        GameMode.NORMAL -> GameModeInfo("🌍", "Klassisch · 60s · Navigation")
        GameMode.PRO -> GameModeInfo("⚡", "Hardcore · 10s · Keine Nav.")
        GameMode.MULTIPLAYER -> GameModeInfo("👥", "Multiplayer-Duell")
        GameMode.ENTDECKER -> GameModeInfo("🧭", "Entspannt · 300s · Navigation")
        GameMode.DETECTIVE -> GameModeInfo("🔍", "Detektiv · 60s · Statisch")
        GameMode.CHALLENGE -> GameModeInfo("🏆", "Vorgefertigte Challenges")
        GameMode.CUSTOM -> GameModeInfo("⚙️", "Individuelle Einstellungen")
        GameMode.BATTLE_ROYALE -> GameModeInfo("👑", "Battle Royale")
    }
}

/**
 * Dialog zur Auswahl des Spielmodus in einer modernen Raster-Ansicht (Grid)
 * mit Icons, Farben und Beschreibungen statt einer einfachen Radio-Button-Liste.
 *
 * @param selectedMode Aktuell ausgewählter Spielmodus.
 * @param onModeSelected Callback bei Auswahl eines verfügbaren Modus.
 * @param onDismiss Schließt den Dialog ohne Änderung.
 */
@Composable
fun GameModeDialog(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Spielmodus auswählen") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameMode.entries
                    .filter { it != GameMode.BATTLE_ROYALE }
                    .chunked(2)
                    .forEach { rowModes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowModes.forEach { mode ->
                                val info = getGameModeInfo(mode)
                                val isSelected = mode == selectedMode

                                ElevatedCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = mode.isAvailable) {
                                            onModeSelected(mode)
                                            onDismiss()
                                        },
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else if (mode.isAvailable) {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        }
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = info.icon, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (mode.isAvailable) mode.displayName else "${mode.displayName} (bald)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = info.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontSize = 10.sp,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                            if (rowModes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}
