package com.example.geoguessr_app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    currentThemeName: String,
    onThemeClick: () -> Unit,
    onStartGameClick: () -> Unit,
    onTutorialClick: () -> Unit,
    hasActiveGame: Boolean,
    onResumeGameClick: () -> Unit,
    onExitAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GeoGuessr",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Entdecke Europa und errate deinen Standort.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            EuropeMenuMap(
                hasActiveGame = hasActiveGame,
                currentThemeName = currentThemeName,
                onStartGameClick = onStartGameClick,
                onResumeGameClick = onResumeGameClick,
                onTutorialClick = onTutorialClick,
                onThemeClick = onThemeClick,
                onExitAppClick = onExitAppClick,
                modifier = Modifier.align(Alignment.Center)
            )

            Text(
                text = "Entwickelt von: Alic Malorodow",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            )
        }
    }
}
