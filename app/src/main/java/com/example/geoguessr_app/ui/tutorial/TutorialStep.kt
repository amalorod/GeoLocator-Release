package com.example.geoguessr_app.ui.tutorial

import androidx.annotation.DrawableRes

/**
 * Ein einzelner Schritt der Tutorial-Klick-Galerie.
 *
 * @property imageRes Platzhalter für einen später einzufügenden
 * Screenshot (z. B. HomeScreen mit rot eingekreistem "Spiel starten"-
 * Button). Bleibt vorerst `null`, bis echte Screenshots vorliegen – die
 * UI zeigt in diesem Fall einen gestrichelten Platzhalter-Rahmen an.
 */
data class TutorialStep(
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int? = null
)

/**
 * Feste Abfolge der Tutorial-Schritte. Zentral an einer Stelle gepflegt,
 * damit sowohl [TutorialScreen] (Aufruf über EuropeMenuMap) als auch der
 * Onboarding-Flow über [WelcomeScreen] dieselbe Galerie zeigen.
 */
val tutorialSteps = listOf(
    TutorialStep(
        title = "Schritt 1: Spiel starten",
        description = "Wähle auf dem Startbildschirm einen Spielmodus aus und tippe auf \"Spiel starten\"."
    ),
    TutorialStep(
        title = "Schritt 2: Standort untersuchen",
        description = "Sieh dich in der Street-View-Umgebung um und suche nach Hinweisen wie Schildern, Vegetation oder Fahrbahnmarkierungen."
    ),
    TutorialStep(
        title = "Schritt 3: Tipp abgeben",
        description = "Markiere den vermuteten Standort auf der Weltkarte und bestätige deinen Tipp."
    ),
    TutorialStep(
        title = "Schritt 4: Punkte sammeln",
        description = "Je näher dein Tipp am tatsächlichen Standort liegt, desto mehr Punkte erhältst du."
    ),
    TutorialStep(
        title = "Schritt 5: Fünf Runden spielen",
        description = "Nach fünf Standorten wird dein Gesamtergebnis angezeigt."
    )
)