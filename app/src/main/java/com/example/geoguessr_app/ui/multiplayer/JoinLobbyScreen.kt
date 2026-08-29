package com.example.geoguessr_app.ui.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun JoinLobbyScreen(
    onJoinClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var lobbyCode by remember { mutableStateOf("") }

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
                    text = "Multiplayer",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "🔑",
                fontSize = 72.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Hast du einen Code?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Gib den 6-stelligen Code ein, den dein Freund erstellt hat.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = lobbyCode,
                onValueChange = {
                    if (it.length <= 6) {
                        lobbyCode = it.uppercase()
                    }
                },
                label = { Text("Lobby-Code") },
                placeholder = { Text("ABC123") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onJoinClick(lobbyCode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                enabled = lobbyCode.length >= 4,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                Text("Jetzt beitreten", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
