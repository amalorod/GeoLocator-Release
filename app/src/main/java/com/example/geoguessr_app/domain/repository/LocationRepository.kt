package com.example.geoguessr_app.domain.repository

import com.example.geoguessr_app.domain.model.GeoLocation

/**
 * Definiert, welche Standortdaten die App benötigt.
 *
 * Die Domain-Schicht kennt nur diese Schnittstelle und nicht die konkrete
 * Herkunft der Daten.
 */
interface LocationRepository {

    suspend fun getLocations(): List<GeoLocation>

    suspend fun getLocationsByIds(ids: List<String>): List<GeoLocation>
}