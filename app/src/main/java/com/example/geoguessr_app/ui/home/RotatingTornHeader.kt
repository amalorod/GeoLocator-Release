package com.example.geoguessr_app.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme

/**
 * Rotierender Bildbereich mit diagonaler, gezackter Abrisskante.
 */
@Composable
fun RotatingTornHeader(
    @DrawableRes imageId: Int,
    modifier: Modifier = Modifier,
    height: Dp = 220.dp,

) {
    val tornShape = TornWallpaperShape()
    val accentColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
           /* .shadow(
                elevation = 12.dp,
                shape = tornShape,
                clip = false // Wichtig: Schatten darf überstehen
            ) */

            .shadow(
                elevation = 16.dp,
                shape = tornShape,
                clip = false,
                ambientColor = accentColor.copy(alpha = 0.75f),
                spotColor = accentColor.copy(alpha = 0.75f)
            )


            .clip(tornShape) // Erst hier wird das Bild im Container final beschnitten
    )


    {

        Crossfade(
            targetState = imageId,
            modifier = Modifier.fillMaxSize(),
            animationSpec = tween(durationMillis = 1_200),
            label = "homeHeaderCrossfade"
        ) { imageId ->
            Image(
                painter = androidx.compose.ui.res.painterResource(imageId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize() // KORREKTUR: Funktioniert jetzt, da es innerhalb der Box liegt
                .background(accentColor.copy(alpha = 0.20f))
        )
    }




}

/**
 * Schneidet die Unterkante diagonal und zackenartig aus.
 */
private class TornWallpaperShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)

            lineTo(size.width, size.height * 0.78f)
            lineTo(size.width * 0.91f, size.height * 0.83f)
            lineTo(size.width * 0.84f, size.height * 0.79f)
            lineTo(size.width * 0.75f, size.height * 0.87f)
            lineTo(size.width * 0.67f, size.height * 0.84f)
            lineTo(size.width * 0.58f, size.height * 0.91f)
            lineTo(size.width * 0.48f, size.height * 0.88f)
            lineTo(size.width * 0.39f, size.height * 0.95f)
            lineTo(size.width * 0.29f, size.height * 0.91f)
            lineTo(size.width * 0.18f, size.height)
            lineTo(0f, size.height * 0.96f)

            close()
        }

        return Outline.Generic(path)
    }
}
