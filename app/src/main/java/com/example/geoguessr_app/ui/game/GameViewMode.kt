package com.example.geoguessr_app.ui.game

/**
 * Bestimmt, welcher interaktive Bereich während einer Runde sichtbar ist.
 *
 * Steuert innerhalb von [GameScreen] die Umschaltung zwischen der
 * Street-View-Ansicht (Erkundung des Standorts) und der Weltkarte
 * (Abgabe des Tipps). Beide Zustände sind exklusiv: Es existiert kein
 * dritter Modus, da eine Runde funktional immer entweder im Erkundungs-
 * oder im Tipp-Abgabe-Schritt ist.
 */
enum class GameViewMode {
    /** Spielende betrachten und erkunden den Street-View-Standort. */
    STREET_VIEW,

    /** Spielende geben ihren Tipp auf der Weltkarte ab. */
    GUESS_MAP
}