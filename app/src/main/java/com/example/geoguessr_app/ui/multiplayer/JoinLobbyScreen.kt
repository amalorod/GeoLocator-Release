package com.example.geoguessr_app.ui.multiplayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun JoinLobbyScreen(
    onJoinClick: (String) -> Unit,
    onBackClick: () -> Unit
) {

    var lobbyCode by remember {
        mutableStateOf("")
    }

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
            text = "Lobby beitreten",
            style =
                MaterialTheme.typography
                    .headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(
            value = lobbyCode,
            onValueChange = {
                lobbyCode = it.uppercase()
            },
            label = {
                Text("Lobby-Code")
            }
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = {
                onJoinClick(lobbyCode)
            }
        ) {
            Text("Beitreten")
        }
    }
}
