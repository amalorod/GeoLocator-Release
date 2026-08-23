package com.example.geoguessr_app.data.location

import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Lokale Repository-Implementierung.
 *
 * Die feste Standortliste ermöglicht zunächst reproduzierbare Entwicklung
 * ohne Netzwerkzugriff. Später können weitere Orte ergänzt werden.
 */
class LocalLocationRepository @Inject constructor() : LocationRepository {

    override suspend fun getLocations(): List<GeoLocation> {
        return ALL_LOCATIONS
    }

    override suspend fun getLocationsByIds(ids: List<String>): List<GeoLocation> {
        return ALL_LOCATIONS.filter { it.id in ids }
    }

    companion object {
        private val ALL_LOCATIONS = listOf(
            GeoLocation(
                id = "berlin",
                name = "Berlin",
                country = "Deutschland",
                latitude = 52.5200,
                longitude = 13.4050,
                hint = "Der Ort befindet sich Nordöstlich Deutschlands"
            ),
            GeoLocation(
                id = "paris",
                name = "Paris",
                country = "Frankreich",
                latitude = 48.8566,
                longitude = 2.3522,
                hint = "Croissants, Kaffee, Eifelturm"
            ),
            GeoLocation(
                id = "rome",
                name = "Rom",
                country = "Italien",
                latitude = 41.9028,
                longitude = 12.4964,
                hint = "Eine damalige Weltmacht"
            ),
            GeoLocation(
                id = "london",
                name = "London",
                country = "Vereinigtes Königreich",
                latitude = 51.5074,
                longitude = -0.1278,
                hint = "Queen __i_____h"
            ),
            GeoLocation(
                id = "madrid",
                name = "Madrid",
                country = "Spanien",
                latitude = 40.4168,
                longitude = -3.7038,
                hint = "Urlaub, Fußball, Real ______"
            ),
            GeoLocation(
                id = "vienna",
                name = "Wien",
                country = "Österreich",
                latitude = 48.2082,
                longitude = 16.3738,
                hint = "Dieses Schnitzel kennt jeder"

            ),
            GeoLocation(
                id = "prague",
                name = "Prag",
                country = "Tschechien",
                latitude = 50.0755,
                longitude = 14.4378,
                hint = "Vepřo-knedlo-zelo"
            ),
            GeoLocation(
                id = "amsterdam",
                name = "Amsterdam",
                country = "Niederlande",
                latitude = 52.3676,
                longitude = 4.9041,
                hint = "Fahrräder, Fahrräder, Fahrräder"
            )
        )
    }
}