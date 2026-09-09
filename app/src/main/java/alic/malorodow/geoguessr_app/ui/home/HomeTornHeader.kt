package alic.malorodow.geoguessr_app.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Bildbereich am oberen Rand des [HomeScreen] mit diagonaler,
 * gezackter "Abrisskante" (siehe [TornWallpaperShape]) und einem farbigen
 * Leuchtschatten in der aktuellen Theme-Akzentfarbe.
 *
 * Der Bildwechsel selbst (welches [imageId] gerade angezeigt wird) wird von
 * [HomeScreen] gesteuert; diese Komponente ist rein für Darstellung und
 * weichen Übergang (via [Crossfade]) zwischen den Bildern verantwortlich.
 *
 * @param imageId Drawable-Resource-ID des aktuell anzuzeigenden Hintergrundbilds.
 * @param modifier Modifier für den äußeren Container.
 * @param height Höhe des Headers; Default passt zur ursprünglichen Gestaltung
 * auf dem Startbildschirm.
 */
@Composable
fun HomeTornHeader(
    @DrawableRes imageId: Int,
    modifier: Modifier = Modifier,
    height: Dp = 220.dp
) {
    val tornShape = TornWallpaperShape()
    val accentColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            // Der Schatten wird vor dem .clip() angewendet und mit
            // clip = false gerendert, damit der farbige Leuchteffekt über
            // die gezackte Kontur hinausragen darf, statt an ihr
            // abgeschnitten zu werden.
            .shadow(
                elevation = 16.dp,
                shape = tornShape,
                clip = false,
                ambientColor = accentColor.copy(alpha = 0.75f),
                spotColor = accentColor.copy(alpha = 0.75f)
            )
            // Erst hier wird der eigentliche Bildinhalt auf die gezackte
            // Form zugeschnitten.
            .clip(tornShape)
    ) {
        Crossfade(
            targetState = imageId,
            modifier = Modifier.fillMaxSize(),
            animationSpec = tween(durationMillis = 1_200),
            label = "homeHeaderCrossfade"
        ) { currentImageId ->
            Image(
                painter = painterResource(currentImageId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        // Halbtransparenter Farbschleier in der aktuellen Akzentfarbe über
        // dem Bild, um dessen Kontrast zu reduzieren und die darüber
        // liegenden Texte/Buttons in HomeScreen besser lesbar zu machen.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(accentColor.copy(alpha = 0.20f))
        )
    }
}

/**
 * Schneidet die Unterkante eines rechteckigen Bereichs diagonal und
 * zackenartig aus, um den Eindruck einer abgerissenen Ecke/Wallpaper-Kante
 * zu erzeugen. Die Eckpunkte sind als relative Größenanteile (0.0–1.0)
 * definiert und wurden empirisch für eine plausible Zackenoptik abgestimmt.
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