package alic.malorodow.geoguessr_app.ui.game

import alic.malorodow.geoguessr_app.ui.home.GameModeDialog
import alic.malorodow.geoguessr_app.ui.multiplayer.LobbyViewModel

/**
 * Konfiguration der verfügbaren Spielmodi.
 *
 * Jeder Modus bündelt sämtliche Regel-Parameter (Rundendauer, Street-View-
 * Navigation, Verfügbarkeit) an einer Stelle, statt diese Werte verstreut
 * im [GameViewModel] oder in der UI abzufragen – das entspricht dem
 * Single Source of Truth - Prinzip für Spielregeln.
 *
 * [BATTLE_ROYALE] ist ausschließlich im Multiplayer-Kontext spielbar und wird
 * daher im regulären Einzelspieler-Auswahldialog
 * ([GameModeDialog]) bewusst herausgefiltert und nicht angezeigt.
 * Die eigene Auswahl erfolgt stattdessen über die Multiplayer-Lobby
 * (siehe [LobbyViewModel]).
 *
 * Das Feld [isAvailable] betrifft daher aktuell keinen Modus mehr, bleibt aber
 * als genereller Mechanismus für künftige, noch nicht fertiggestellte Modi
 * bestehen.
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
    Ausschließlich über die Multiplayer-Lobby wählbar,
    nicht über den Einzelspieler-Dialog (siehe [GameModeDialog]).
     */
    BATTLE_ROYALE(
        displayName = "Battle Royale",
        roundDurationSeconds = 30,
        streetViewNavigationEnabled = true,
        isAvailable = true
    ),

    /** Mehrspieler-Standardmodus ohne Elimination (siehe [BATTLE_ROYALE] für die Elimination-Variante). */
    MULTIPLAYER(
        displayName = "Multiplayer",
        roundDurationSeconds = 60,
        streetViewNavigationEnabled = true,
        isAvailable = true
    ),

    /** Entspanntes Erkunden ohne Hektik mit großzügigem Zeitlimit und voller Navigation. */
    ENTDECKER(
        displayName = "Entdecker",
        roundDurationSeconds = 300,
        streetViewNavigationEnabled = true,
        isAvailable = true
    ),

    /** Detektiv-Modus: normale Zeit, aber keine freie Bewegung (statische Beobachtung). */
    DETECTIVE(
        displayName = "Detektiv",
        roundDurationSeconds = 60,
        streetViewNavigationEnabled = false,
        isAvailable = true
    ),

    /** Vorgefertigte Herausforderungen mit spezifischen Regeln und Regionen. */
    CHALLENGE(
        displayName = "Challenges",
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