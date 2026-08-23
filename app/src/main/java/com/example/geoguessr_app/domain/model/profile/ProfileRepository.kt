package com.example.geoguessr_app.data.profile

import com.example.geoguessr_app.domain.model.profile.PlayerProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ProfileRepository {

    private val _profile =
        MutableStateFlow(
            PlayerProfile(
                playerId = "LOCAL_PLAYER",
                playerName = "Alic Malorodow"
            )
        )

    val profile: StateFlow<PlayerProfile> =
        _profile.asStateFlow()

    fun updateProfile(
        profile: PlayerProfile
    ) {
        _profile.value = profile
    }
}