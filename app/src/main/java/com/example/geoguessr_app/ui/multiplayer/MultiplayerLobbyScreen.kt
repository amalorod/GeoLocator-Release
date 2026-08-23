package com.example.geoguessr_app.ui.multiplayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackClick
            ) {
                Text("Zurück")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLeaveClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                )
            ) {
                Text("Verlassen")
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Lobby",
            style = MaterialTheme
                .typography
                .headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lobbyCode,
            onValueChange = {},
            readOnly = true,
            label = { Text("Lobby-Code") },
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(lobbyCode))
                }) {
                    Text("Kopieren")
                }
            }
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(32.dp))
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
            // Normaler Lobby-Inhalt (nur anzeigen wenn kein Fehler)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${players.size} / 4 Spieler"
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

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
                            text =
                            if (player.host)
                                "${player.name} 👑"
                            else
                                player.name,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = if (player.ready) "BEREIT ✅" else "Warten... ⏳",
                            color = if (player.ready) Color.Green else Color.Gray,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Spielmodus"
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            MultiplayerMode.entries
                .forEach { mode ->

                    ModeButton(
                        mode = mode,
                        selected =
                        mode == selectedMode,
                        onClick = {
                            if (isHost) onModeSelected(mode)
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .height(8.dp)
                    )
                }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onReadyClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReady) Color.DarkGray else Color.Blue
                )
            ) {
                Text(if (isReady) "Nicht bereit" else "Bereit")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isHost) {
                val allReady = players.size >= 2 && players.all { it.ready }

                Button(
                    onClick = onStartGameClick,
                    enabled = allReady,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allReady) Color.Red else Color.Gray
                    )
                ) {
                    Text("Spiel starten")
                }
            } else {
                Text(
                    text = "Warte auf Host...",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodySmall
                )
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
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor =
                if (selected)
                    Color.Red
                else
                    Color.DarkGray
        )
    ) {
        Text(mode.displayName)
    }
}