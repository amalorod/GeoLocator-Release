package com.example.geoguessr_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.ui.theme.AppThemeMode


/** Mindestbreite/-höhe der Dropdown-Einträge für ausreichend große Touch-Targets. */
private val THEME_MENU_ITEM_MIN_WIDTH: Dp = 190.dp
private val THEME_MENU_ITEM_MIN_HEIGHT: Dp = 56.dp

/** Feste Maße der Kachel-Vorschau: rechteckiger Block mit horizontalen Farbstreifen. */
private val THEME_TILE_WIDTH: Dp = 100.dp
private val THEME_TILE_HEIGHT: Dp = 64.dp
private val THEME_TILE_CORNER_RADIUS: Dp = 16.dp

/**
 * Einzelne Auswahlkachel im Theme-Grid: Ein rechteckiger Block mit
 * abgerundeten Ecken, gefüllt mit vier horizontalen Farbstreifen in
 * abgestuften Tönen des jeweiligen Themes ([AppThemeMode.previewStripeColors]),
 * darunter der Name des Themes. Ist [isSelected] gesetzt, wird die Kachel
 * zusätzlich mit einem Rahmen hervorgehoben, damit der aktuell aktive
 * Zustand auch ohne Fließtext klar erkennbar ist.
 *
 * @param theme Das darzustellende Farbschema.
 * @param isSelected Ob dieses Theme aktuell aktiv ist (und dynamicColor
 * dabei deaktiviert ist – wird vom Aufrufer bereits entsprechend geprüft).
 * @param onClick Callback bei Auswahl dieser Kachel.
 */
@Composable
private fun ThemeGridTile(
    theme: AppThemeMode, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            // Feste Breite statt defaultMinSize/weight: verhindert, dass die
            // Kachel sich an die verfügbare Breite des DropdownMenu anpasst
            // und dabei die Farbstreifen in die Länge zieht. Da DropdownMenu
            // seine eigene Breite an der intrinsischen Breite des Inhalts
            // ausrichtet, bleibt durch diese feste Breite auch das Menü
            // insgesamt kompakt statt auf Bildschirmbreite zu expandieren.
            .width(THEME_TILE_WIDTH)
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(THEME_TILE_HEIGHT)
                .clip(RoundedCornerShape(THEME_TILE_CORNER_RADIUS))
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(THEME_TILE_CORNER_RADIUS)
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            theme.previewStripeColors().forEach { stripeColor ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(stripeColor)
                )
            }
        }

        Text(
            text = theme.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Wiederverwendbarer Auslöser-Button mit angehängtem Dropdown-Menü zur
 * Auswahl des App-weiten Farbschemas. Wird identisch sowohl in [AppTopBar]
 * (GameScreen) als auch in [EuropeMenuMap] (Teil des HomeScreens)
 * eingebunden, damit die Auswahllogik nur einmal existiert und beide Screens
 * dasselbe Bedienkonzept anbieten.
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

        // Einziger DropdownMenu-Container: enthält den Systemfarbe-Switch
        // und darunter das Theme-Grid. Der frühere zweite, separate
        // AppThemeMode.entries.forEach-Block mit Punkt+Text-Einträgen wurde
        // entfernt, da er sich mit dem neuen LazyVerticalGrid überlagerte
        // (zwei parallele Darstellungen derselben Auswahl im selben Menü).
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

            // DropdownMenu wickelt seinen Inhalt
            // bereits selbst in eine scrollbare Column ein. Ein zusätzliches
            // Lazy-Layout darin führt unabhängig von heightIn()/Modifier-
            // Beschränkungen zu einer IllegalStateException ("Vertically
            // scrollable component was measured with an infinity maximum height
            // constraints"), da zwei vertikal scrollbare Container ineinander
            // verschachtelt werden. Da hier ohnehin nur 5 feste Themes existieren,
            // reicht ein einfaches, nicht-lazy Grid aus Row/Column vollkommen aus
            // und vermeidet dieses Nesting-Problem komplett.
            val themeRows = AppThemeMode.entries.chunked(2)
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                themeRows.forEach { rowThemes ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowThemes.forEach { theme ->
                            ThemeGridTile(
                                theme = theme,
                                isSelected = theme == currentTheme && !currentDynamicColorEnabled,
                                onClick = {
                                    onThemeSelected(theme)
                                    isThemeMenuExpanded = false
                                }
                            )
                        }
                        // Füllt die letzte, ungerade Zeile mit derselben festen Breite
                        // wie eine echte Kachel auf, damit das Layout symmetrisch bleibt,
                        // ohne dass sich die Zeile über die restliche Breite streckt.
                        if (rowThemes.size < 2) {
                            Box(modifier = Modifier.width(THEME_TILE_WIDTH))
                        }
                    }
                }
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
 *
 * Wird ausschließlich für den Auslöser-Button (Vorschaufarbe im Trigger)
 * verwendet; für die Kachel-Darstellung im Grid selbst kommt stattdessen
 * [previewStripeColors] zum Einsatz.
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

/**
 * Liefert vier tonal abgestufte Vorschaufarben je Farbschema, die als
 * horizontale Streifen innerhalb einer Kachel dargestellt werden. Analog
 * zu [AppThemeMode.previewColor] bewusst hart kodiert und unabhängig von
 * den tatsächlichen Material3-ColorSchemes in Theme.kt, da hier eine
 * stabile, gut unterscheidbare visuelle Vorschau im Vordergrund steht statt
 * einer exakten Wiedergabe der echten App-Farben.
 *
 * Die vier Töne gehen jeweils vom dunkelsten (Index 0) zum hellsten
 * (Index 3) Farbwert des jeweiligen Themes.
 */
private fun AppThemeMode.previewStripeColors(): List<Color> {
    return when (this) {
        AppThemeMode.LIGHT -> listOf(
            Color(0xFFB8AEDB), Color(0xFFD0C8EE), Color(0xFFE6E0F7), Color(0xFFF2EFFF)
        )

        AppThemeMode.DARK -> listOf(
            Color(0xFF16141A), Color(0xFF1E1B22), Color(0xFF29252E), Color(0xFF3A343F)
        )

        AppThemeMode.BEIGE -> listOf(
            Color(0xFF795548), Color(0xFF9C7A62), Color(0xFFB08968), Color(0xFFD8C3A5)
        )

        AppThemeMode.BLUE -> listOf(
            Color(0xFF1565C0), Color(0xFF1E88E5), Color(0xFF42A5F5), Color(0xFF90CAF9)
        )

        AppThemeMode.ROSE -> listOf(
            Color(0xFFAD466D), Color(0xFFD987A5), Color(0xFFE8B4C8), Color(0xFFF6DDE6)
        )
    }
}