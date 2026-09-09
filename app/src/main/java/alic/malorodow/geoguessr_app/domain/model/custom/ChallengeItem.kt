package alic.malorodow.geoguessr_app.domain.model.custom

/**
 * Repräsentiert eine vorgefertigte Challenge im Challenge-Modus (siehe
 * GameMode.CHALLENGE). Kapselt eine Beschreibung sowie die zugehörigen
 * CustomGameSettings, mit denen die Partie gestartet wird.
 */
data class ChallengeItem(
    val title: String,
    val description: String,
    val icon: String,
    val settings: CustomGameSettings
)