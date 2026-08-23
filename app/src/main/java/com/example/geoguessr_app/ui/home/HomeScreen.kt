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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.geoguessr_app.ui.game.GameMode
import kotlinx.coroutines.delay

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn

@Composable
fun HomeScreen(
    selectedGameMode: GameMode,
    onGameModeSelected: (GameMode) -> Unit,
    currentThemeName: String,
    onThemeClick: () -> Unit,
    onStartGameClick: () -> Unit,
    onTutorialClick: () -> Unit,
    hasActiveGame: Boolean,
    onResumeGameClick: () -> Unit,
    onExitAppClick: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit,
    onStatisticsClick: () -> Unit,
) {


    val headerImages = listOf(
        R.drawable.home_header_berlin,
        R.drawable.home_header_france,
        R.drawable.home_header_italy
    )
    var isModeDialogVisible by remember {
        mutableStateOf(false)
    }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600


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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 700.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
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
                            text = "Entdecke Europa und errate deinen Standort!",
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
                            .offset(y = if (isTablet) 40.dp else 0.dp)
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                    )

                    Button(
                        onClick = {
                            isModeDialogVisible = true
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 52.dp)
                            .width(350.dp)
                            .height(58.dp),
                        shape = CompassBannerShape(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "MODUS: ${selectedGameMode.displayName.uppercase()}",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 130.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        HomeShortcutButton(
                            icon = "👤",
                            onClick = onProfileClick
                        )

                        HomeShortcutButton(
                            icon = "📊",
                            onClick = onStatisticsClick
                        )
                    }

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
    }


if (isModeDialogVisible) {
    GameModeDialog(
        selectedMode = selectedGameMode,
        onModeSelected = onGameModeSelected,
        onDismiss = {
            isModeDialogVisible = false
        }
    )
}
}

/**
 * Langgezogene Wegweiserform mit Spitzen an beiden Seiten.
 */
private class CompassBannerShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(size.width * 0.08f, 0f)
            lineTo(size.width * 0.92f, 0f)
            lineTo(size.width, size.height * 0.50f)
            lineTo(size.width * 0.92f, size.height)
            lineTo(size.width * 0.08f, size.height)
            lineTo(0f, size.height * 0.50f)
            close()
        }

        return Outline.Generic(path)
    }
}


@Composable
private fun HomeShortcutButton(
    icon: String,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
            }
        }
    }
}