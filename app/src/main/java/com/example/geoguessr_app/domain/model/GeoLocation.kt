package com.example.geoguessr_app.domain.model

import com.example.geoguessr_app.domain.model.custom.Region

/**
 * Repräsentiert einen spielbaren Standort.
 *
 * Das Domain-Modell kennt weder Android noch Compose oder Google Maps.
 * Dadurch bleibt die Spiellogik unabhängig von technischen Details.
 */
data class GeoLocation(
    val id: String,
    val name: String,
    val country: String,
    val region: Region,
    val latitude: Double,
    val hint: String,
    val longitude: Double
)