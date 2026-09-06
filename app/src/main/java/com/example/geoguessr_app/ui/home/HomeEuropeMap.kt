package com.example.geoguessr_app.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun HomeEuropeMap(
    @DrawableRes backgroundImageId: Int,
    modifier: Modifier = Modifier
) {
    val europeShape = remember { EuropeShape() }
    val accentColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            // Der Schatten nutzt nicht die exakte EuropeShape,
            // sondern eine stark abgerundete, konvexe Annäherung: echte
            // Elevation-/Blur-Schatten (Modifier.shadow) berechnen ihre
            // Outline zuverlässig nur für konvexe Formen. Bei der stark
            // einspringenden, sternförmigen EuropeShape würde der Schatten
            // fälschlich nach innen statt nach außen wirken. Der Inhalt
            // selbst wird weiterhin exakt mit EuropeShape geclippt, nur
            // der Schattenwurf nutzt die vereinfachte Form.
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(50),
                clip = false,
                ambientColor = accentColor.copy(alpha = 0.75f),
                spotColor = accentColor.copy(alpha = 0.85f)
            )
            .clip(europeShape)
            .border(
                width = 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor,
                        MaterialTheme.colorScheme.tertiary,
                        accentColor
                    )
                ),
                shape = europeShape
            )
    ) {
        Crossfade(
            targetState = backgroundImageId,
            animationSpec = tween(durationMillis = 1_200),
            label = "europeImageCrossfade",
            modifier = Modifier.fillMaxSize()
        ) { targetImageId ->
            Image(
                painter = painterResource(targetImageId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.42f,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(700f, 700f)
                    )
                )
        )
    }
}

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