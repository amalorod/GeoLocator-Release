package com.example.geoguessr_app.data.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

object ProfilePreferences {

    val PLAYER_ID =
        stringPreferencesKey("player_id")

    val PLAYER_NAME =
        stringPreferencesKey("player_name")
}