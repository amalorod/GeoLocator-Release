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
    selectedMode: MultiplayerMode,
    errorMessage: String? = null,
    onModeSelected: (MultiplayerMode) -> Unit,
    onBackClick: () -> Unit,
    onStartGameClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Button(
            onClick = onBackClick
        ) {
            Text("Zurück")
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
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

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

                Text(
                    text =
                        if (player.isHost)
                            "${player.name} 👑"
                        else
                            player.name,
                    modifier = Modifier.padding(16.dp)
                )
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
                        onModeSelected(mode)
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
            onClick = onStartGameClick,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("Spiel starten")
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