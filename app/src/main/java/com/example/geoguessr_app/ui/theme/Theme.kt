package com.example.geoguessr_app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

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
 * Wählt anhand von [themeMode] eines der fünf festen, selbst definierten
 * Farbschemata aus – außer [dynamicColor] ist aktiviert und das Gerät läuft
 * mindestens auf Android 12 (API 31, Build.VERSION_CODES.S): In diesem Fall
 * wird stattdessen die vom System aus dem Wallpaper abgeleitete
 * Material-You-Farbpalette verwendet ([dynamicLightColorScheme]/
 * [dynamicDarkColorScheme]). Auf älteren Android-Versionen ist dynamicColor
 * technisch nicht verfügbar, weshalb dort trotz aktiviertem Flag automatisch
 * auf [themeMode] zurückgefallen wird.
 *
 * @param themeMode Vom Nutzer gewähltes, festes Farbschema (Hell, Dunkel, Beige, Blau, Rosé).
 * @param dynamicColor Ob die Material-You-Systemfarbe anstelle von [themeMode] verwendet werden soll.
 * @param darkTheme Steuert bei aktivem dynamicColor, ob die helle oder dunkle
 * Systempalette verwendet wird; per Default aus dem aktuellen System-Modus abgeleitet.
 * @param content Der Composable-Inhalt, auf den das Theme angewendet wird.
 */
@Composable
fun GeoGuessr_AppTheme(
    themeMode: AppThemeMode,
    dynamicColor: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> when (themeMode) {
            AppThemeMode.LIGHT -> LightColorScheme
            AppThemeMode.DARK -> DarkColorScheme
            AppThemeMode.BEIGE -> BeigeColorScheme
            AppThemeMode.BLUE -> BlueColorScheme
            AppThemeMode.ROSE -> RoseColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}