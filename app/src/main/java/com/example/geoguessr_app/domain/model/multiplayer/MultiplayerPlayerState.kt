package com.example.geoguessr_app.domain.model.multiplayer

data class MultiplayerPlayerState(

    val uid: String = "",

    val playerName: String = "",

    val score: Int = 0,

    val round: Int = 1,

    val lives: Int = 5,

    val finishedRound: Boolean = false,

    val currentGuessScore: Int = 0

)