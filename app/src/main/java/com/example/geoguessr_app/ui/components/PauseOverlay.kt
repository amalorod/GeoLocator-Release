package com.example.geoguessr_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

/**
 * Vollflächiges, halbtransparentes Overlay während einer Pause im
 * Spielbildschirm ([com.example.geoguessr_app.ui.game.GameScreen]).
 *
 * Verdunkelt den gesamten Bildschirminhalt darunter (Street View bleibt
 * dabei im Hintergrund unverändert im Compose-Baum
 * erhalten, nur optisch überdeckt). Ein Tipp auf eine beliebige Stelle
 * innerhalb des Overlays löst [onResume] aus und setzt die Partie fort.
 *
 * @param onResume Setzt die pausierte Partie fort (typischerweise [GameViewModel.resumeGame()]).
 * @param modifier Modifier für den äußeren Container.
 */
@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(onClick = onResume)
            // contentDescription macht klar, dass ein Tipp die
            // Partie fortsetzt.
            .semantics { contentDescription = "Pausiert. Zum Fortsetzen tippen." },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ⅱ",
            color = Color.White,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )
    }
}