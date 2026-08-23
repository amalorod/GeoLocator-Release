package com.example.geoguessr_app.domain.model.multiplayer

data class MatchSession(

    val sessionId: String = "",
    val hostUid: String = "",
    val lobbyCode: String = "",

    val mode: String = "",

    val currentRound: Int = 1,

    val totalRounds: Int = 5,

    val currentLocationId: String = "",

    val roundStartTimestamp: Long = 0L,

    val started: Boolean = false,

    val finished: Boolean = false,

    val locationIds: List<String> = emptyList()
)