package alic.malorodow.geoguessr_app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import alic.malorodow.geoguessr_app.ui.theme.AppThemeMode
import alic.malorodow.geoguessr_app.ui.home.HomeEuropeMap

/**
 * Gemeinsame Steuerleiste für den Spielbildschirm: Rücksprung zur Startseite,
 * Pausieren der laufenden Runde sowie Öffnen des [ThemeSelectorMenu]s.
 *
 * Die eigentliche Theme-Auswahl-Logik ist nach [ThemeSelectorMenu]
 * ausgelagert, da dieselbe Auswahl auch im HomeScreen
 * ([HomeEuropeMap]) angeboten wird und so nicht doppelt
 * implementiert werden muss.
 */
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    currentTheme: AppThemeMode,
    currentDynamicColorEnabled: Boolean,
    isPauseEnabled: Boolean,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDynamicColorToggled: (Boolean) -> Unit,
    onHomeClick: () -> Unit,
    onPauseClick: () -> Unit,
    showPauseButton: Boolean = true,
    onPauseGame: () -> Unit,
) {

    val actionButtonColors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onHomeClick, modifier = Modifier.weight(1f), colors = actionButtonColors) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home"
            )
        }

        if (showPauseButton) {
            Button(
                onClick = onPauseClick,
                enabled = isPauseEnabled,
                modifier = Modifier.weight(1f),
                colors = actionButtonColors
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause"
                )
            }
        }

        ThemeSelectorMenu(
            currentTheme = currentTheme,
            currentDynamicColorEnabled = currentDynamicColorEnabled,
            onThemeSelected = onThemeSelected,
            onDynamicColorToggled = onDynamicColorToggled,
            modifier = Modifier.weight(1f)
        )
    }
}