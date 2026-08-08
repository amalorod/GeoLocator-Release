package com.example.geoguessr_app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Startbildschirm der Anwendung.
 *
 * Der Screen enthält keine Navigationslogik. Stattdessen meldet er
 * Benutzeraktionen über Callback-Funktionen an die darüberliegende
 * Navigationsschicht. Dadurch bleibt die UI unabhängig und testbar.
 */
@Composable
fun HomeScreen(
    onStartGameClick: () -> Unit,
    onTutorialClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "GeoGuessr",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Entdecke die Welt und errate deinen Standort.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onStartGameClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Spiel starten")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onTutorialClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Spielanleitung")
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Entwickelt von: Alic Malorodow",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}