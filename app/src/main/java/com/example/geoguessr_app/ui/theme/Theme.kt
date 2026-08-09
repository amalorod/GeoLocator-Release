package com.example.geoguessr_app.ui.theme



import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme

import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val BeigeColorScheme = lightColorScheme(
    primary = Color(0xFF795548),
    secondary = Color(0xFF9C7A62),
    background = Color(0xFFFFF8E7),
    surface = Color(0xFFFFF8E7),
    onPrimary = Color.White,
    onBackground = Color(0xFF352A24),
    onSurface = Color(0xFF352A24)
)

private val BlueColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    secondary = Color(0xFF42A5F5),
    background = Color(0xFFEAF4FF),
    surface = Color(0xFFF4F9FF),
    onPrimary = Color.White,
    onBackground = Color(0xFF102A43),
    onSurface = Color(0xFF102A43)
)

private val RoseColorScheme = lightColorScheme(
    primary = Color(0xFFAD466D),
    secondary = Color(0xFFD987A5),
    background = Color(0xFFFFF0F5),
    surface = Color(0xFFFFF7FA),
    onPrimary = Color.White,
    onBackground = Color(0xFF462733),
    onSurface = Color(0xFF462733)
)

@Composable
fun GeoGuessr_AppTheme(
    themeMode: AppThemeMode,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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