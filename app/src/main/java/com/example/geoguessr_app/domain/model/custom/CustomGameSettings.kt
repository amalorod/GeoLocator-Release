package com.example.geoguessr_app.domain.model.custom

import com.example.geoguessr_app.navigation.AppDestination.IndividualSettings
import com.example.geoguessr_app.navigation.GeoGuessrNavHost
import com.example.geoguessr_app.ui.game.GameViewModel
import com.example.geoguessr_app.ui.game.IndividualSettingsScreen

/**
 * Kapselt sämtliche vom Nutzer konfigurierbaren Parameter des
 * individuellen Spielmodus (siehe Doku, Kapitel 5.3 „Individueller
 * Modus“).
 *
 * Als data class mit Standardwerten für jeden Parameter modelliert,
 * damit im [IndividualSettingsScreen] eine vollständig gültige Instanz
 * existiert, sobald der Nutzer die Einstellungen öffnet. Die Standardwerte
 * entsprechen dabei bewusst einem ausgewogenen Einstiegs-Setup (60
 * Sekunden Zeitlimit, weltweiter Standortpool, mittlerer
 * Schwierigkeitsgrad).
 *
 * Diese Klasse wird an [GameViewModel.startCustomGame] übergeben und
 * steuert dort, wie die Spielrunde aufgebaut wird (siehe [GeoGuessrNavHost],
 * [IndividualSettings]-Route).
 *
 * @property timeLimitSeconds Verfügbare Zeit pro Runde in Sekunden.
 * @property region Geografische Region, auf die der Standortpool
 *   eingeschränkt wird (siehe [Region]). WORLD bedeutet keine
 *   Einschränkung.
 * @property difficulty Schwierigkeitsgrad der Partie (siehe
 *   [CustomDifficulty]), beeinflusst die Option Street-View-Navigation.
 * @property rounds Anzahl der zu spielenden Runden in der Partie.
 */
data class CustomGameSettings(
    val timeLimitSeconds: Int = 60,
    val region: Region = Region.WORLD,
    val difficulty: CustomDifficulty = CustomDifficulty.MEDIUM,
    val rounds: Int = 5
)