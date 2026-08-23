package com.example.geoguessr_app.ui.multiplayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WaitingForPlayersOverlay() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        ElevatedCard {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                CircularProgressIndicator()

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    "Warten auf Spieler..."
                )
            }
        }
    }
}
