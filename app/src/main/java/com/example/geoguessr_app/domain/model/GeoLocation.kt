package com.example.geoguessr_app.domain.model

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
    val latitude: Double,
    val longitude: Double
)