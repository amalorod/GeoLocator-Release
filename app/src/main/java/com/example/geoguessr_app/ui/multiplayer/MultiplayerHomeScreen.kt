package com.example.geoguessr_app.ui.multiplayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.navigation.AppDestination

@Composable
fun MultiplayerHomeScreen(
    onCreateLobbyClick: () -> Unit,
    onJoinLobbyClick: () -> Unit,
    onBackClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Button(
            onClick = onBackClick
        ) {
            Text("Zurück")
        }

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Text(
            text = "Multiplayer",
            style =
                MaterialTheme.typography
                    .headlineMedium
        )

        Spacer(
            modifier = Modifier.height(48.dp)
        )

        Button(
            onClick = onCreateLobbyClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lobby erstellen")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = onJoinLobbyClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lobby beitreten")
        }
    }
}