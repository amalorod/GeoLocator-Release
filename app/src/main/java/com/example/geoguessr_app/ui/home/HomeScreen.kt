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
import com.example.geoguessr_app.R
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    currentThemeName: String,
    onThemeClick: () -> Unit,
    onStartGameClick: () -> Unit,
    onTutorialClick: () -> Unit,
    hasActiveGame: Boolean,
    onResumeGameClick: () -> Unit,
    onExitAppClick: () -> Unit,
    modifier: Modifier = Modifier,
) {


    val headerImages = listOf(
        R.drawable.home_header_berlin,
        R.drawable.home_header_france,
        R.drawable.home_header_italy
    )

    var currentImageIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    LaunchedEffect(currentImageIndex) {
        delay(4_000L)
        currentImageIndex = (currentImageIndex + 1) % headerImages.size
    }

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            RotatingTornHeader(
                imageId = headerImages[currentImageIndex],
                height = 230.dp,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = 42.dp,
                        start = 20.dp,
                        end = 20.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GeoGuessr",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer // Ensure readability over images
                )

                Text(
                    text = "Entdecke Europa und errate deinen Standort.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            EuropeMenuMap(
                backgroundImageId = headerImages[currentImageIndex],
                hasActiveGame = hasActiveGame,
                currentThemeName = currentThemeName,
                onStartGameClick = onStartGameClick,
                onResumeGameClick = onResumeGameClick,
                onTutorialClick = onTutorialClick,
                onThemeClick = onThemeClick,
                onExitAppClick = onExitAppClick,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp)
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
