package com.example.geoguessr_app.ui.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Erklärt die grundlegende Spielsteuerung.
 *
 * Der Inhalt ist scrollbar, damit er auch auf kleinen Geräten
 * vollständig erreichbar bleibt.
 */
@Composable
fun TutorialScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "So funktioniert das Spiel",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        TutorialStep(
            number = 1,
            title = "Standort untersuchen",
            description = "Sieh dich in der Umgebung um und suche nach Hinweisen."
        )

        TutorialStep(
            number = 2,
            title = "Tipp abgeben",
            description = "Markiere den vermuteten Standort auf der Weltkarte."
        )

        TutorialStep(
            number = 3,
            title = "Punkte sammeln",
            description = "Je näher dein Tipp am tatsächlichen Standort liegt, " +
                    "desto mehr Punkte erhältst du."
        )

        TutorialStep(
            number = 4,
            title = "Fünf Runden spielen",
            description = "Nach fünf Standorten wird dein Gesamtergebnis angezeigt."
        )

        Button(onClick = onBackClick) {
            Text(text = "Zurück zum Start")
        }
    }
}

/**
 * Wiederverwendbare Darstellung eines Tutorial-Schritts.
 */
@Composable
private fun TutorialStep(
    number: Int,
    title: String,
    description: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$number. $title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}