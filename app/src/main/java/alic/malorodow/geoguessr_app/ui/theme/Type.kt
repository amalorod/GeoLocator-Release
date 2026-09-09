package alic.malorodow.geoguessr_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Zentrale Typografie-Definition für das App-weite Material 3 Theme.
 *
 * Aktuell wird lediglich [Typography.bodyLarge] individuell gesetzt; alle übrigen
 * Textrollen (titleLarge, labelSmall, ...) verwenden bewusst die
 * Material-3-Standardwerte, um den Definitionsaufwand gering zu halten
 * und ein konsistentes Erscheinungsbild mit dem Betriebssystem zu wahren.
 */
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)