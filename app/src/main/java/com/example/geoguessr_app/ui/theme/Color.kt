package com.example.geoguessr_app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Zentrale Farbdefinitionen für das Material 3 Farbschema.
 *
 * Die Suffixe 80/40 folgen der Material-Design-Konvention für Tonwerte:
 * höhere Werte (80) stehen für hellere, im Dark Theme verwendete Akzent-
 * farben, niedrigere Werte (40) für kräftigere Farben im Light Theme.
 * Alle projektspezifischen Farbschemata (Beige, Blau, Rosé) werden direkt
 * in [Theme.kt] über [androidx.compose.material3.Color]-Literale definiert
 * und referenzieren diese Basisfarben nicht weiter.
 */

// Dark Theme – helle Akzentfarben für ausreichenden Kontrast auf dunklem Grund
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

// Light Theme – kräftigere Akzentfarben für Kontrast auf hellem Grund
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)