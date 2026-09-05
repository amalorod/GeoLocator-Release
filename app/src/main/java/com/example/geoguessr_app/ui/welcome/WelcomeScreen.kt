package com.example.geoguessr_app.ui.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import com.example.geoguessr_app.ui.tutorial.TutorialScreen
import kotlinx.coroutines.delay

/**
 * Erster Bildschirm, den ein Nutzer beim allerersten App-Start sieht
 * (gesteuert über [OnboardingDataStoreRepository.hasSeenTutorial] in
 * [GeoGuessrNavHost]).
 *
 * Ablauf: Zeigt kurz einen Begrüßungstext, der per Fly-out-Animation
 * seitlich herausgleitet ([showWelcomeText]), und blendet danach direkt
 * in [TutorialScreen] über – der Nutzer landet also nahtlos in der
 * Klick-Galerie, ohne zusätzliche Navigation. Erst nach vollständigem
 * Durchklicken des Tutorials (letzter Button "Home") wird
 * [onOnboardingFinished] ausgelöst.
 *
 * @param onOnboardingFinished Markiert das Tutorial als gesehen und
 * navigiert zum Home-Screen (siehe [GeoGuessrNavHost]).
 */
@Composable
fun WelcomeScreen(
    onOnboardingFinished: () -> Unit
) {
    var showWelcomeText by remember { mutableStateOf(true) }

    // Zeigt den Begrüßungstext für 1.5 Sekunden, bevor die Fly-out-
    // Animation gestartet wird, die anschließend das Tutorial freigibt.
    LaunchedEffect(Unit) {
        delay(1500)
        showWelcomeText = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = showWelcomeText,
            enter = fadeIn(),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 600)
            ) + fadeOut(animationSpec = tween(durationMillis = 600))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Willkommen zu",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "GeoGuessr!",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }

        AnimatedVisibility(
            visible = !showWelcomeText,
            enter = fadeIn(animationSpec = tween(durationMillis = 400))
        ) {
            TutorialScreen(
                onFinish = onOnboardingFinished,
                onBackClick = null,
                isWelcomeFlow = true
            )
        }
    }
}