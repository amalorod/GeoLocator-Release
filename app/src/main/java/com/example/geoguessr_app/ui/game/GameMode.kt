package com.example.geoguessr_app.ui.game

/**
 * Konfiguration der verfügbaren Spielmodi.
 *
 * Jeder Modus bündelt sämtliche Regel-Parameter (Rundendauer, Street-View-
 * Navigation, Verfügbarkeit) an einer Stelle, statt diese Werte verstreut
 * im [GameViewModel] oder in der UI abzufragen – das entspricht dem
 * Single-Source-of-Truth-Prinzip für Spielregeln.
 *
 * Noch nicht implementierte Modi (aktuell [BATTLE_ROYALE]) werden bereits im
 * Auswahlmenü ([com.example.geoguessr_app.ui.home.GameModeDialog]) angezeigt,
 * bleiben dort aber deaktiviert und mit "bald verfügbar" gekennzeichnet,
 * statt komplett ausgeblendet zu werden.
 *
 * @property displayName Für die UI bestimmter, deutschsprachiger Anzeigename.
 * @property roundDurationSeconds Zeitlimit pro Runde in Sekunden.
 * @property streetViewNavigationEnabled Ob sich Spielende im Street-View-Panorama
 * frei bewegen/drehen dürfen; bei false (siehe [PRO]) ist nur das Betrachten,
 * nicht die Navigation erlaubt (Hardcore-Charakter).
 * @property isAvailable Ob der Modus aktuell spielbar ist oder nur als
 * Vorschau in der Modusauswahl erscheint.
 */
enum class GameMode(
    val displayName: String,
    val roundDurationSeconds: Int,
    val streetViewNavigationEnabled: Boolean,
    val isAvailable: Boolean
) {
    /** Standardmodus mit voller Street-View-Navigation und moderatem Zeitlimit. */
    NORMAL(
        displayName = "Normal",
        roundDurationSeconds = 60,
        streetViewNavigationEnabled = true,
        isAvailable = true
    ),

    /** Hardcore-Variante: kein freies Navigieren im Panorama, stark verkürztes Zeitlimit. */
    PRO(
        displayName = "Pro",
        roundDurationSeconds = 10,
        streetViewNavigationEnabled = false,
        isAvailable = true
    ),

    /**
     * Mehrspieler-Elimination-Modus. Noch nicht implementiert
     * ([isAvailable] = false); Parameter sind bereits als Platzhalter für
     * die spätere Umsetzung hinterlegt.
     */
    BATTLE_ROYALE(
        displayName = "Battle Royale",
        roundDurationSeconds = 30,
        streetViewNavigationEnabled = true,
        isAvailable = false
    ),

    /** Mehrspieler-Standardmodus ohne Elimination (siehe [BATTLE_ROYALE] für die Elimination-Variante). */
    MULTIPLAYER(
        displayName = "Multiplayer",
        roundDurationSeconds = 60,
        streetViewNavigationEnabled = true,
        isAvailable = true
    ),

    /** Modus mit täglich wechselnden, festen Standorten (siehe Daily-Quest-System). */
    DAILY_QUEST(
        displayName = "Daily Quest",
        roundDurationSeconds = 60,
        streetViewNavigationEnabled = true,
        isAvailable = true
    ),

    /** Modus mit vom Spielenden frei konfigurierbaren Einstellungen (Region, Schwierigkeit). */
    CUSTOM(
        displayName = "Individuell",
        roundDurationSeconds = 60,
        streetViewNavigationEnabled = true,
        isAvailable = true
    )
}