package com.example.geoguessr_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Farbschema für den Dark Mode. Verwendet die für dunkle Hintergründe
 * optimierten "80er"-Tonwerte aus [Color.kt].
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/**
 * Standard-Farbschema für den Light Mode. Verwendet die kräftigeren
 * "40er"-Tonwerte. Weitere Material-Farbrollen (background, surface,
 * onPrimary, ...) werden bewusst nicht überschrieben und fallen auf die
 * von Material 3 generierten Standardwerte zurück.
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Warmes, beiges Farbschema als alternative Theme-Option.
 * Definiert zusätzlich background/surface/onPrimary/onBackground/onSurface
 * explizit, da diese Rollen visuell stark vom Standard-Material-Look
 * abweichen sollen (warme Beige-/Brauntöne statt neutralem Grau).
 */
private val BeigeColorScheme = lightColorScheme(
    primary = Color(0xFF795548),
    secondary = Color(0xFF9C7A62),
    tertiary = Color(0xFFB08968),
    background = Color(0xFFFFF8E7),
    surface = Color(0xFFFFF8E7),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF352A24),
    onSurface = Color(0xFF352A24)
)

/**
 * Kühles, blaues Farbschema als alternative Theme-Option.
 */
private val BlueColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    secondary = Color(0xFF42A5F5),
    tertiary = Color(0xFF64B5F6),
    background = Color(0xFFEAF4FF),
    surface = Color(0xFFF4F9FF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF102A43),
    onSurface = Color(0xFF102A43)
)

/**
 * Warmes Rosé-Farbschema als alternative Theme-Option.
 */
private val RoseColorScheme = lightColorScheme(
    primary = Color(0xFFAD466D),
    secondary = Color(0xFFD987A5),
    tertiary = Color(0xFFE8B4C8),
    background = Color(0xFFFFF0F5),
    surface = Color(0xFFFFF7FA),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF462733),
    onSurface = Color(0xFF462733)
)

/**
 * Wurzel-Composable für das App-weite Material 3 Theme.
 *
 * Wählt anhand des übergebenen [themeMode] das passende Farbschema aus
 * und stellt es zusammen mit der zentralen [Typography] über
 * [MaterialTheme] allen untergeordneten Composables zur Verfügung. Da
 * [AppThemeMode] bereits DARK/LIGHT sowie drei weitere feste Paletten
 * kapselt, wird der Systemzustand [darkTheme] hier bewusst nicht mehr
 * zur automatischen Umschaltung genutzt, sondern nur als Default-Wert
 * für Aufrufer bereitgehalten, die kein explizites Theme setzen.
 *
 * @param themeMode Vom Nutzer gewähltes Farbschema (Hell, Dunkel, Beige, Blau, Rosé).
 * @param darkTheme Fallback-Indikator für System-Dark-Mode; wird aktuell
 * nicht zur Schema-Auswahl verwendet, da diese ausschließlich über [themeMode] erfolgt.
 * @param content Der Composable-Inhalt, auf den das Theme angewendet wird.
 */
@Composable
fun GeoGuessr_AppTheme(
    themeMode: AppThemeMode,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.BEIGE -> BeigeColorScheme
        AppThemeMode.BLUE -> BlueColorScheme
        AppThemeMode.ROSE -> RoseColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}