// domain/model/custom/PredefinedChallenges.kt
package com.example.geoguessr_app.domain.model.custom

/**
 * Statische, im Code fest definierte Sammlung an vorgefertigten
 * Challenges für den Challenge-Modus der App.
 *
 * Gehört zur Domain-Layer im Sinne von Clean Architecture: Diese
 * Objekt-Deklaration enthält reine, unveränderliche Fachdaten
 * ([ChallengeItem]) ohne Abhängigkeit zu Android-Framework-Klassen,
 * UI oder Persistenz. Dadurch bleibt sie unabhängig testbar und
 * wiederverwendbar in Compose-Screens als auch in ViewModels.
 *
 * Jede Challenge kombiniert Anzeige-Informationen (Titel, Beschreibung,
 * Icon) mit den tatsächlichen Spieleinstellungen ([CustomGameSettings]),
 * die beim Start der Challenge direkt an die Spiellogik übergeben werden.
 */
object PredefinedChallenges {

    /**
     * Vollständige Liste aller vordefinierten Challenges, die im
     * [ChallengeSelectionScreen] zur Auswahl angezeigt werden.
     *
     * Jeder Eintrag deckt eine andere Kombination aus Region,
     * Schwierigkeitsgrad und Zeitlimit ab, um unterschiedliche
     * Spielerfahrungen ohne manuelle Konfiguration durch den Nutzer
     * anzubieten.
     */
    val all: List<ChallengeItem> = listOf(
        ChallengeItem(
            title = "Nordamerika Challenge",
            description = "30 Sekunden Zeitlimit, Fokus auf Nordamerika.",
            icon = "🗽",
            settings = CustomGameSettings(
                timeLimitSeconds = 30,
                region = Region.NORTH_AMERICA,
                difficulty = CustomDifficulty.MEDIUM
            )
        ),
        ChallengeItem(
            title = "Europe Master",
            description = "30 Sekunden Zeitlimit, fokussiert auf Europa im schweren Schwierigkeitsgrad (keine freie Navigation).",
            icon = "🏰",
            settings = CustomGameSettings(
                timeLimitSeconds = 30,
                region = Region.EUROPE,
                difficulty = CustomDifficulty.HARD
            )
        ),
        ChallengeItem(
            title = "Südamerika-Profi",
            description = "60 Sekunden Zeitlimit, Erkundung von Südamerika mit mittlerem Schwierigkeitsgrad.",
            icon = "🌴",
            settings = CustomGameSettings(
                timeLimitSeconds = 60,
                region = Region.SOUTH_AMERICA,
                difficulty = CustomDifficulty.MEDIUM
            )
        ),
        ChallengeItem(
            title = "Global Speed Challenge",
            description = "Weltweite Standorte mit schnellen 20 Sekunden pro Runde.",
            icon = "⚡",
            settings = CustomGameSettings(
                timeLimitSeconds = 20,
                region = Region.WORLD,
                difficulty = CustomDifficulty.EASY
            )
        )
    )
}