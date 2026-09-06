package com.example.geoguessr_app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.ui.components.ThemeSelectorMenu
import com.example.geoguessr_app.ui.theme.AppThemeMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Alle interaktiven Buttons, die über [HomeEuropeMap]
 * gelegt werden (Start, Fortsetzen, Tutorial, Theme, Beenden). Als
 * [BoxScope]-Erweiterung modelliert, damit `.align(...)` weiterhin
 * funktioniert, obwohl Silhouette und Buttons jetzt in getrennten Dateien
 * liegen – beide werden in [HomeScreen] als Geschwister im selben Box
 * aufgerufen.
 */
@Composable
fun BoxScope.HomeMapActionButtons(
    hasActiveGame: Boolean,
    isTablet: Boolean,
    currentTheme: AppThemeMode,
    currentDynamicColorEnabled: Boolean,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDynamicColorToggled: (Boolean) -> Unit,
    onStartGameClick: () -> Unit,
    onResumeGameClick: () -> Unit,
    onTutorialClick: () -> Unit,
    onExitAppClick: () -> Unit,
) {

    // Auf Tablets rücken die Buttons weiter Richtung Bildschirmrand statt
    // dicht an der Kartenmitte zu kleben – die Karte selbst bleibt über
    // widthIn(max = 600.dp) in HomeScreen unverändert kompakt.
    val edgeOffset = if (isTablet) 48.dp else 0.dp
    val startButtonWidth = if (isTablet) 190.dp else 158.dp
    val endButtonWidth = if (isTablet) 190.dp else 158.dp
    val bottomButtonWidth = if (isTablet) 180.dp else 150.dp


    AnimatedVisibility(
        enter = fadeIn() + expandIn(),
        exit = fadeOut() + shrinkOut(),
        visible = hasActiveGame,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = 18.dp)
    ) {
        CountryMenuButton(
            text = "Spiel fortsetzen",
            onClick = onResumeGameClick,
            modifier = Modifier
                .width(190.dp)
                .countryMotion(seed = 1)
                .height(64.dp),
            shape = RoundedCornerShape(
                topStartPercent = 55, topEndPercent = 25,
                bottomEndPercent = 50, bottomStartPercent = 20
            )
        )
    }

    CountryMenuButton(
        text = "Spiel starten",
        onClick = onStartGameClick,
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = -edgeOffset, y = (-60).dp)
            .width(startButtonWidth)
            .countryMotion(seed = 7)
            .height(78.dp),
        shape = RoundedCornerShape(
            topStartPercent = 20, topEndPercent = 60,
            bottomEndPercent = 25, bottomStartPercent = 50
        )
    )

    CountryMenuButton(
        text = "Spielanleitung",
        onClick = onTutorialClick,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .offset(x = edgeOffset, y = 20.dp)
            .width(endButtonWidth)
            .countryMotion(seed = 6)
            .height(74.dp),
        shape = RoundedCornerShape(
            topStartPercent = 50, topEndPercent = 25,
            bottomEndPercent = 55, bottomStartPercent = 25
        )
    )

    ThemeSelectorMenu(
        currentTheme = currentTheme,
        currentDynamicColorEnabled = currentDynamicColorEnabled,
        onThemeSelected = onThemeSelected,
        onDynamicColorToggled = onDynamicColorToggled,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .offset(x = -edgeOffset, y = (-45).dp)
            .width(bottomButtonWidth)
            .countryMotion(seed = 6)
            .height(72.dp),
        buttonContent = { expandMenu, previewColor, label ->
            CountryMenuButton(
                text = "Theme\n$label",
                onClick = expandMenu,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(
                    topStartPercent = 60, topEndPercent = 25,
                    bottomEndPercent = 35, bottomStartPercent = 15
                )
            )
        }
    )

    CountryMenuButton(
        text = "App beenden",
        onClick = onExitAppClick,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = edgeOffset, y = (-18).dp)
            .width(bottomButtonWidth)
            .countryMotion(seed = 5)
            .height(72.dp),
        shape = RoundedCornerShape(
            topStartPercent = 25, topEndPercent = 55,
            bottomEndPercent = 15, bottomStartPercent = 45
        )
    )
}

@Composable
private fun CountryMenuButton(
    text: String, onClick: () -> Unit, shape: RoundedCornerShape, modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick, modifier = modifier, shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = text, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Modifier.countryMotion(seed: Int): Modifier {
    val transition = rememberInfiniteTransition(label = "countryMotion$seed")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6_000 + seed * 650, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "countryPhase$seed"
    )
    return graphicsLayer {
        translationX = sin(phase + seed) * 6.dp.toPx()
        translationY = cos(phase + seed) * 5.dp.toPx()
        rotationZ = sin(phase + seed) * 1.5f
        scaleX = 1f + sin(phase) * 0.018f
        scaleY = 1f + cos(phase) * 0.022f
    }
}