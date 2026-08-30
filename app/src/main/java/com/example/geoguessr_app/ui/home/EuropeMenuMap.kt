package com.example.geoguessr_app.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.ui.components.ThemeSelectorMenu
import com.example.geoguessr_app.ui.theme.AppThemeMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Stilisierte Europakarte mit gut erreichbaren Menüflächen.
 */
@Composable
fun EuropeMenuMap(
    @DrawableRes backgroundImageId: Int,
    hasActiveGame: Boolean,
    currentTheme: AppThemeMode,
    currentDynamicColorEnabled: Boolean,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDynamicColorToggled: (Boolean) -> Unit,
    onStartGameClick: () -> Unit,
    onResumeGameClick: () -> Unit,
    onTutorialClick: () -> Unit,
    onExitAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(modifier = modifier.height(430.dp)) {
        val europeShape = remember { EuropeShape() }

        // Crossfade sorgt für einen weichen Übergang zwischen den rotierenden
        // Hintergrundbildern aus HomeScreen, statt eines harten Bildwechsels.
        Crossfade(
            targetState = backgroundImageId,
            animationSpec = tween(durationMillis = 1_200),
            label = "europeImageCrossfade",
            modifier = Modifier
                .fillMaxSize()
                .clip(europeShape)
        ) { targetImageId ->
            Image(
                painter = painterResource(targetImageId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.42f,
                modifier = Modifier.fillMaxSize()
            )
        }

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
                    topStartPercent = 55,
                    topEndPercent = 25,
                    bottomEndPercent = 50,
                    bottomStartPercent = 20
                )
            )
        }

        CountryMenuButton(
            text = "Spiel starten",
            onClick = onStartGameClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(y = (-60).dp)
                .width(158.dp)
                .countryMotion(seed = 7)
                .height(78.dp),
            shape = RoundedCornerShape(
                topStartPercent = 20,
                topEndPercent = 60,
                bottomEndPercent = 25,
                bottomStartPercent = 50
            )
        )

        CountryMenuButton(
            text = "Spielanleitung",
            onClick = onTutorialClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(y = 20.dp)
                .width(158.dp)
                .countryMotion(seed = 6)
                .height(74.dp),
            shape = RoundedCornerShape(
                topStartPercent = 50,
                topEndPercent = 25,
                bottomEndPercent = 55,
                bottomStartPercent = 25
            )
        )


        // Ersetzt den bisherigen reinen onThemeClick()-Button: Dasselbe
        // Dropdown-Menü wie in AppTopBar wird hier mit der individuellen
        // "Länderflächen"-Optik als Auslöser wiederverwendet, statt eine
        // zweite, eigene Auswahllogik zu pflegen.
        ThemeSelectorMenu(
            currentTheme = currentTheme,
            currentDynamicColorEnabled = currentDynamicColorEnabled,
            onThemeSelected = onThemeSelected,
            onDynamicColorToggled = onDynamicColorToggled,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = (-45).dp)
                .width(150.dp)
                .countryMotion(seed = 6)
                .height(72.dp),
            buttonContent = { expandMenu, previewColor, label ->
                CountryMenuButton(
                    text = "Theme\n$label",
                    onClick = expandMenu,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(
                        topStartPercent = 60,
                        topEndPercent = 25,
                        bottomEndPercent = 35,
                        bottomStartPercent = 15
                    )
                )
            })

        CountryMenuButton(
            text = "App beenden",
            onClick = onExitAppClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-18).dp)
                .width(150.dp)
                .countryMotion(seed = 5)
                .height(72.dp),
            shape = RoundedCornerShape(
                topStartPercent = 25,
                topEndPercent = 55,
                bottomEndPercent = 15,
                bottomStartPercent = 45
            )
        )


    }
}

/**
 * Einzelner, unregelmäßig geformter Menü-Button innerhalb der Europakarte.
 * Die Form wird pro Aufrufstelle individuell über [RoundedCornerShape] mit
 * unterschiedlichen Eckenradien gestaltet, um den Eindruck einzelner
 * "Länderflächen" statt gleichförmiger Buttons zu erzeugen.
 */
@Composable
private fun CountryMenuButton(
    text: String, onClick: () -> Unit, shape: RoundedCornerShape, modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick, modifier = modifier, shape = shape, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = text, textAlign = TextAlign.Center
        )
    }
}

/**
 * Erzeugt eine langsame, organische Schwebe-Bewegung (Translation, leichte
 * Rotation und Skalierung) für einen Menü-Button. Unterschiedliche [seed]-Werte
 * sorgen für phasenverschobene, individuelle Bewegungsmuster je Button, damit
 * nicht alle Flächen synchron "pulsieren" und einen unnatürlichen Eindruck erzeugen.
 *
 * @param seed Frei wählbarer Ganzzahlwert zur Phasenverschiebung; unterschiedliche
 * Buttons sollten unterschiedliche Seeds erhalten.
 */
@Composable
private fun Modifier.countryMotion(
    seed: Int
): Modifier {
    val transition = rememberInfiniteTransition(
        label = "countryMotion$seed"
    )

    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(), animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 6_000 + seed * 650, easing = LinearEasing
            ), repeatMode = RepeatMode.Restart
        ), label = "countryPhase$seed"
    )

    return graphicsLayer {
        translationX = sin(phase + seed) * 6.dp.toPx()
        translationY = cos(phase + seed) * 5.dp.toPx()
        rotationZ = sin(phase + seed) * 1.5f

        // Unterschiedliche Skalierung simuliert ein sanftes Morphing.
        scaleX = 1f + sin(phase) * 0.018f
        scaleY = 1f + cos(phase) * 0.022f
    }
}

/**
 * Dekorativer, stark vereinfachter Umriss Europas als Freiform-Clip-Maske
 * für den rotierenden Hintergrund. Die Eckpunkte sind manuell als relative
 * Größenanteile (0.0–1.0) gesetzt und wurden empirisch für ein plausibles,
 * kontinent-ähnliches Silhouettenbild abgestimmt.
 */
private class EuropeShape : Shape {

    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(size.width * 0.18f, size.height * 0.18f)
            lineTo(size.width * 0.42f, size.height * 0.08f)
            lineTo(size.width * 0.66f, size.height * 0.15f)
            lineTo(size.width * 0.82f, size.height * 0.30f)
            lineTo(size.width * 0.72f, size.height * 0.50f)
            lineTo(size.width * 0.88f, size.height * 0.68f)
            lineTo(size.width * 0.62f, size.height * 0.82f)
            lineTo(size.width * 0.47f, size.height * 0.95f)
            lineTo(size.width * 0.36f, size.height * 0.72f)
            lineTo(size.width * 0.13f, size.height * 0.62f)
            lineTo(size.width * 0.25f, size.height * 0.42f)
            close()
        }

        return Outline.Generic(path)
    }
}
