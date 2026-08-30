package com.example.geoguessr_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.ui.theme.AppThemeMode

/** Mindestbreite/-höhe der Dropdown-Einträge für ausreichend große Touch-Targets. */
private val THEME_MENU_ITEM_MIN_WIDTH: Dp = 190.dp
private val THEME_MENU_ITEM_MIN_HEIGHT: Dp = 56.dp

/**
 * Wiederverwendbarer Auslöser-Button mit angehängtem Dropdown-Menü zur
 * Auswahl des App-weiten Farbschemas. Wird identisch sowohl in [AppTopBar]
 * (Spielbildschirm) als auch in
 * [com.example.geoguessr_app.ui.home.EuropeMenuMap] (Startbildschirm)
 * eingebunden, damit die Auswahllogik nur einmal existiert und beide Screens
 * garantiert dasselbe Bedienkonzept anbieten.
 *
 * Der Button-Inhalt (Farbpunkt + Text) kann über [buttonContent] individuell
 * gestaltet werden, da HomeScreen und GameScreen unterschiedliche
 * Trigger-Optiken benötigen (z. B. eigene Button-Form auf dem HomeScreen);
 * das Dropdown-Menü selbst bleibt dabei identisch.
 *
 * @param currentTheme Aktuell aktives, festes Farbschema.
 * @param currentDynamicColorEnabled Ob stattdessen die Material-You-Systemfarbe verwendet wird.
 * @param onThemeSelected Callback bei Auswahl eines festen Farbschemas.
 * @param onDynamicColorToggled Callback beim Umschalten der Material-You-Systemfarbe.
 * @param modifier Modifier für den äußeren Container (Button + Dropdown-Anker).
 * @param buttonContent Optik des sichtbaren Auslöser-Buttons; erhält den
 * aktuellen Auswahlzustand, um z. B. Farbpunkt und Label passend anzuzeigen.
 */
@Composable
fun ThemeSelectorMenu(
    currentTheme: AppThemeMode,
    currentDynamicColorEnabled: Boolean,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDynamicColorToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    buttonContent: @Composable (
        expandMenu: () -> Unit, previewColor: Color, label: String
    ) -> Unit = { expandMenu, previewColor, label ->
        DefaultThemeSelectorButton(onClick = expandMenu, previewColor = previewColor, label = label)
    }
) {
    var isThemeMenuExpanded by remember { mutableStateOf(false) }

    val previewColor = if (currentDynamicColorEnabled) {
        MaterialTheme.colorScheme.primary
    } else {
        currentTheme.previewColor()
    }
    val label = "Design"

    Box(modifier = modifier) {
        buttonContent(
            { isThemeMenuExpanded = true }, previewColor, label
        )

        DropdownMenu(
            expanded = isThemeMenuExpanded, onDismissRequest = { isThemeMenuExpanded = false }) {
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Systemfarbe",
                            fontWeight = if (currentDynamicColorEnabled) FontWeight.Bold else FontWeight.Normal
                        )
                        Switch(
                            checked = currentDynamicColorEnabled,
                            onCheckedChange = onDynamicColorToggled
                        )
                    }
                },
                onClick = { onDynamicColorToggled(!currentDynamicColorEnabled) },
                modifier = Modifier.defaultMinSize(
                    minWidth = THEME_MENU_ITEM_MIN_WIDTH, minHeight = THEME_MENU_ITEM_MIN_HEIGHT
                )
            )

            HorizontalDivider()

            AppThemeMode.entries.forEach { theme ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(color = theme.previewColor(), shape = CircleShape)
                            )
                            Text(
                                text = theme.displayName,
                                fontWeight = if (theme == currentTheme && !currentDynamicColorEnabled) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    }, onClick = {
                        onThemeSelected(theme)
                        isThemeMenuExpanded = false
                    }, modifier = Modifier.defaultMinSize(
                        minWidth = THEME_MENU_ITEM_MIN_WIDTH, minHeight = THEME_MENU_ITEM_MIN_HEIGHT
                    )
                )
            }
        }
    }
}

/** Standard-Optik des Auslöser-Buttons, wie bisher in AppTopBar verwendet. */
@Composable
private fun DefaultThemeSelectorButton(
    onClick: () -> Unit, previewColor: Color, label: String
) {
    val colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = colors) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color = previewColor, shape = CircleShape)
            )
            Text(label)
        }
    }
}

/**
 * Liefert eine feste, repräsentative Vorschaufarbe je Farbschema. Bewusst
 * unabhängig von [com.example.geoguessr_app.ui.theme.Theme.kt] hart kodiert,
 * da sie eine stabile visuelle Wiedererkennung liefern soll, unabhängig vom
 * aktuell angewendeten MaterialTheme.
 */
private fun AppThemeMode.previewColor(): Color {
    return when (this) {
        AppThemeMode.LIGHT -> Color(0xFFF2EFFF)
        AppThemeMode.DARK -> Color(0xFF29252E)
        AppThemeMode.BEIGE -> Color(0xFFD8C3A5)
        AppThemeMode.BLUE -> Color(0xFF42A5F5)
        AppThemeMode.ROSE -> Color(0xFFD987A5)
    }
}