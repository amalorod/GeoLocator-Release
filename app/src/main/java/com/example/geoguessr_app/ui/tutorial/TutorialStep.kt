package com.example.geoguessr_app.ui.tutorial

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.geoguessr_app.R

/**
 * Ein einzelner Schritt der Tutorial-Klick-Galerie.
 *
 * @property imageRes Platzhalter für einen später einzufügenden
 * Screenshot (z. B. HomeScreen mit rot eingekreistem "Spiel starten"-
 * Button). Bleibt vorerst `null`, bis echte Screenshots vorliegen – die
 * UI zeigt in diesem Fall einen gestrichelten Platzhalter-Rahmen an.
 */
data class TutorialStep(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val imageRes: Int? = null
)

/**
 * Feste Abfolge der Tutorial-Schritte. Zentral an einer Stelle gepflegt,
 * damit sowohl [TutorialScreen] (Aufruf über EuropeMenuMap) als auch der
 * Onboarding-Flow über [WelcomeScreen] dieselbe Galerie zeigen.
 */
val tutorialSteps = listOf(
    TutorialStep(
        titleRes = R.string.tutorial_title_1,
        descriptionRes = R.string.tutorial_desc_1,
        imageRes = R.drawable.tutorial_screen_1
    ),
    TutorialStep(
        titleRes = R.string.tutorial_title_2,
        descriptionRes = R.string.tutorial_desc_2,
        imageRes = R.drawable.tutorial_screen_2
    ),
    TutorialStep(
        titleRes = R.string.tutorial_title_3,
        descriptionRes = R.string.tutorial_desc_3,
        imageRes = R.drawable.tutorial_screen_3
    ),
    TutorialStep(
        titleRes = R.string.tutorial_title_4,
        descriptionRes = R.string.tutorial_desc_4,
        imageRes = R.drawable.tutorial_screen_4
    ),
    TutorialStep(
        titleRes = R.string.tutorial_title_5,
        descriptionRes = R.string.tutorial_desc_5,
        imageRes = R.drawable.tutorial_screen_5
    )
)
