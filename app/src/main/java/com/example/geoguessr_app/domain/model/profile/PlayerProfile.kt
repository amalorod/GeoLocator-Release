package com.example.geoguessr_app.domain.model.profile

import com.google.firebase.database.PropertyName

data class PlayerProfile(
    @get:PropertyName("playerId")
    @set:PropertyName("playerId")
    var playerId: String = "",
    
    @get:PropertyName("playerName")
    @set:PropertyName("playerName")
    var playerName: String = "Spieler",
    
    @get:PropertyName("profileImageUrl")
    @set:PropertyName("profileImageUrl")
    var profileImageUrl: String? = null,
    
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Long = 0L
)
