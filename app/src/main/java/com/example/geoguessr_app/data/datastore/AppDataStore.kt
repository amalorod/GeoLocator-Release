package com.example.geoguessr_app.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(
    name = "geoguessr_preferences"
)