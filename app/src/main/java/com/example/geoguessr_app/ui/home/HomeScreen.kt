package com.example.geoguessr_app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.R
import com.example.geoguessr_app.ui.game.GameMode
import com.example.geoguessr_app.ui.theme.AppThemeMode
import kotlinx.coroutines.delay

private const val HEADER_ROTATION_INTERVAL_MS = 4_000L
private const val TABLET_BREAKPOINT_DP = 600

// Bilder innerhalb der EuropeMap
private val HEADER_IMAGES = listOf(
    R.drawable.map_picture_1,
    R.drawable.map_picture_2,
    R.drawable.map_picture_3,
    R.drawable.map_picture_4,
    R.drawable.map_picture_5,
    R.drawable.map_picture_6,
    R.drawable.map_picture_7,
    R.drawable.map_picture_8,
    R.drawable.map_picture_9,
)

/**
 * Hauptbildschirm der App. Reiner Orchestrator: hält nur den rein
 * visuellen UI-Zustand (Bildrotation, Modus-Dialog-Sichtbarkeit) und
 * setzt [HomeTornHeader], [HomeEuropeMap], [HomeMapActionButtons],
 * [HomeGameModeButton] und [HomeShortcutButtonsRow] zusammen. Fachliche
 * Zustände werden  ausschließlich über Parameter/Callbacks
 * gereicht.
 */
@Composable
fun HomeScreen(
    selectedGameMode: GameMode,
    onGameModeSelected: (GameMode) -> Unit,
    currentTheme: AppThemeMode,
    currentDynamicColorEnabled: Boolean,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDynamicColorToggled: (Boolean) -> Unit,
    isProfileSetup: Boolean,
    onStartGameClick: () -> Unit,
    onTutorialClick: () -> Unit,
    hasActiveGame: Boolean,
    onResumeGameClick: () -> Unit,
    onExitAppClick: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onDailyQuestClick: () -> Unit,
) {
    var isModeDialogVisible by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= TABLET_BREAKPOINT_DP

    var currentImageIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(currentImageIndex) {
        delay(HEADER_ROTATION_INTERVAL_MS)
        currentImageIndex = (currentImageIndex + 1) % HEADER_IMAGES.size
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 700.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    HomeTornHeader(
                        imageId = HEADER_IMAGES[currentImageIndex],
                        height = 230.dp,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 42.dp, start = 20.dp, end = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GeoGuessr",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Entdecke die Welt und errate den Standort!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = if (isTablet) 40.dp else 0.dp)
                            .widthIn(max = if (isTablet) 800.dp else 600.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(430.dp)
                    ) {
                        HomeEuropeMap(
                            backgroundImageId = HEADER_IMAGES[currentImageIndex],
                            modifier = Modifier.fillMaxSize()
                        )
                        HomeMapActionButtons(
                            hasActiveGame = hasActiveGame,
                            isTablet = isTablet,
                            currentTheme = currentTheme,
                            currentDynamicColorEnabled = currentDynamicColorEnabled,
                            onThemeSelected = onThemeSelected,
                            onDynamicColorToggled = onDynamicColorToggled,
                            onStartGameClick = onStartGameClick,
                            onResumeGameClick = onResumeGameClick,
                            onTutorialClick = onTutorialClick,
                            onExitAppClick = onExitAppClick,
                        )
                    }

                    HomeGameModeButton(
                        selectedGameMode = selectedGameMode,
                        onClick = { isModeDialogVisible = true }
                    )

                    HomeShortcutButtonsRow(
                        isProfileSetup = isProfileSetup,
                        onProfileClick = onProfileClick,
                        onStatisticsClick = onStatisticsClick,
                        onDailyQuestClick = onDailyQuestClick
                    )

                    Text(
                        text = "Entwickelt von: Alic Malorodow",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 15.dp)
                    )
                }
            }
        }

        if (isModeDialogVisible) {
            GameModeDialog(
                selectedMode = selectedGameMode,
                onModeSelected = onGameModeSelected,
                onDismiss = { isModeDialogVisible = false }
            )
        }
    }
}