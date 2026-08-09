package com.example.geoguessr_app.domain.model

/**
 * Repräsentiert eine geografische Position in Dezimalgraden.
 *
 * Breitengrad:
 * -90 Grad bis +90 Grad
 *
 * Längengrad:
 * -180 Grad bis +180 Grad
 */
data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) {
            "Der Breitengrad muss zwischen -90 und 90 liegen."
        }

        require(longitude in -180.0..180.0) {
            "Der Längengrad muss zwischen -180 und 180 liegen."
        }
    }
}