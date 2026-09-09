package alic.malorodow.geoguessr_app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import alic.malorodow.geoguessr_app.ui.game.GameMode

/**
 * Modus-Auswahl-Button öffnet [GameModeDialog]. Als
 * [BoxScope]-Erweiterung, damit die Positionierung innerhalb des
 * umgebenden Box-Bereichs in [HomeScreen] erhalten bleibt.
 */
@Composable
fun BoxScope.HomeGameModeButton(
    selectedGameMode: GameMode,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
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
}

/**
 * Die drei runden Kurzzugriffs-Buttons (Profil, Statistik, Daily Quest)
 * unterhalb des Modus-Buttons.
 */
@Composable
fun BoxScope.HomeShortcutButtonsRow(
    isProfileSetup: Boolean,
    onProfileClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onDailyQuestClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 130.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        HomeShortcutButton(
            icon = "👤",
            onClick = onProfileClick,
            statusColor = if (isProfileSetup) Color.Green else Color.Red
        )
        HomeShortcutButton(icon = "📊", onClick = onStatisticsClick)
        HomeShortcutButton(icon = "📅", onClick = onDailyQuestClick)
    }
}

@Composable
private fun HomeShortcutButton(
    icon: String,
    onClick: () -> Unit,
    statusColor: Color? = null
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = icon, fontSize = 28.sp, textAlign = TextAlign.Center)
            }
        }
        if (statusColor != null) {
            Surface(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp),
                shape = CircleShape,
                color = statusColor,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
            ) {}
        }
    }
}

private class CompassBannerShape : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
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