// domain/model/custom/PredefinedChallenges.kt
package com.example.geoguessr_app.domain.model.custom

object PredefinedChallenges {
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