package com.example.geoguessr_app.ui.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geoguessr_app.domain.model.multiplayer.LobbyPlayer
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerMode

@Composable
fun MultiplayerLobbyScreen(
    lobbyCode: String,
    players: List<LobbyPlayer>,
    currentUserUid: String,
    selectedMode: MultiplayerMode,
    errorMessage: String? = null,
    onModeSelected: (MultiplayerMode) -> Unit,
    onBackClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onReadyClick: () -> Unit,
    onStartGameClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val isHost = players.find { it.uid == currentUserUid }?.host ?: false
    val isReady = players.find { it.uid == currentUserUid }?.ready ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Einheitlicher Header
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
                    text = "Lobby",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = onLeaveClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Verlassen")
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            if (errorMessage != null) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zurück zum Hauptmenü")
                }
            } else {
                Text(
                    text = "Lobby-Code kopieren",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                OutlinedTextField(
                    value = lobbyCode,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = {
                            clipboardManager.setText(AnnotatedString(lobbyCode))
                        }) {
                            Text("Kopieren")
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Spieler (${players.size} / 4)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                players.forEach { player ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (player.host) "${player.name} 👑" else player.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (player.uid == currentUserUid) FontWeight.Bold else FontWeight.Normal
                            )

                            Text(
                                text = if (player.ready) "BEREIT ✅" else "Warten... ⏳",
                                color = if (player.ready) Color(0xFF2E7D32) else Color.Gray,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Spielmodus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                MultiplayerMode.entries.forEach { mode ->
                    ModeButton(
                        mode = mode,
                        selected = mode == selectedMode,
                        onClick = { if (isHost) onModeSelected(mode) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onReadyClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReady) Color.DarkGray else Color(0xFF1565C0)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(if (isReady) "Nicht bereit" else "Ich bin bereit!", style = MaterialTheme.typography.titleMedium)
                }

                if (isHost) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val allReady = players.size >= 2 && players.all { it.ready }
                    Button(
                        onClick = onStartGameClick,
                        enabled = allReady,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allReady) Color(0xFFC62828) else Color.Gray
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Spiel starten", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    Text(
                        text = "Warte auf den Host...",
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    mode: MultiplayerMode,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 4.dp else 0.dp)
    ) {
        Text(mode.displayName, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
