package com.example.geoguessr_app.data.location

import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.model.custom.Region
import com.example.geoguessr_app.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Lokale Repository-Implementierung.
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
                region = Region.EUROPE,
                latitude = 52.5200,
                longitude = 13.4050,
                hint = "Der Ort befindet sich Nordöstlich Deutschlands"
            ),
            GeoLocation(
                id = "paris",
                name = "Paris",
                country = "Frankreich",
                region = Region.EUROPE,
                latitude = 48.8566,
                longitude = 2.3522,
                hint = "Croissants, Kaffee, Eifelturm"
            ),
            GeoLocation(
                id = "rome",
                name = "Rom",
                country = "Italien",
                region = Region.EUROPE,
                latitude = 41.9028,
                longitude = 12.4964,
                hint = "Eine damalige Weltmacht"
            ),
            GeoLocation(
                id = "london",
                name = "London",
                country = "Vereinigtes Königreich",
                region = Region.EUROPE,
                latitude = 51.5074,
                longitude = -0.1278,
                hint = "Queen __i_____h"
            ),
            GeoLocation(
                id = "madrid",
                name = "Madrid",
                country = "Spanien",
                region = Region.EUROPE,
                latitude = 40.4168,
                longitude = -3.7038,
                hint = "Urlaub, Fußball, Real ______"
            ),
            GeoLocation(
                id = "vienna",
                name = "Wien",
                country = "Österreich",
                region = Region.EUROPE,
                latitude = 48.2082,
                longitude = 16.3738,
                hint = "Dieses Schnitzel kennt jeder"
            ),
            GeoLocation(
                id = "prague",
                name = "Prag",
                country = "Tschechien",
                region = Region.EUROPE,
                latitude = 50.0755,
                longitude = 14.4378,
                hint = "Vepřo-knedlo-zelo"
            ),
            GeoLocation(
                id = "amsterdam",
                name = "Amsterdam",
                country = "Niederlande",
                region = Region.EUROPE,
                latitude = 52.3676,
                longitude = 4.9041,
                hint = "Fahrräder, Fahrräder, Fahrräder"
            ),
            GeoLocation(
                id = "new_york",
                name = "New York",
                country = "USA",
                region = Region.NORTH_AMERICA,
                latitude = 40.7128,
                longitude = -74.0060,
                hint = "Freiheitsstatue und Wolkenkratzer"
            ),
            GeoLocation(
                id = "los_angeles",
                name = "Los Angeles",
                country = "USA",
                region = Region.NORTH_AMERICA,
                latitude = 34.0522,
                longitude = -118.2437,
                hint = "Hollywood"
            ),
            GeoLocation(
                id = "chicago",
                name = "Chicago",
                country = "USA",
                region = Region.NORTH_AMERICA,
                latitude = 41.8781,
                longitude = -87.6298,
                hint = "Windy City"
            ),
            GeoLocation(
                id = "toronto",
                name = "Toronto",
                country = "Kanada",
                region = Region.NORTH_AMERICA,
                latitude = 43.6532,
                longitude = -79.3832,
                hint = "CN Tower"
            ),
            GeoLocation(
                id = "vancouver",
                name = "Vancouver",
                country = "Kanada",
                region = Region.NORTH_AMERICA,
                latitude = 49.2827,
                longitude = -123.1207,
                hint = "Pazifikküste Kanadas"
            ),
            GeoLocation(
                id = "mexico_city",
                name = "Mexiko-Stadt",
                country = "Mexiko",
                region = Region.NORTH_AMERICA,
                latitude = 19.4326,
                longitude = -99.1332,
                hint = "Azteken und Tacos"
            ),
            GeoLocation(
                id = "rio",
                name = "Rio de Janeiro",
                country = "Brasilien",
                region = Region.SOUTH_AMERICA,
                latitude = -22.9068,
                longitude = -43.1729,
                hint = "Christusstatue"
            ),
            GeoLocation(
                id = "sao_paulo",
                name = "São Paulo",
                country = "Brasilien",
                region = Region.SOUTH_AMERICA,
                latitude = -23.5505,
                longitude = -46.6333,
                hint = "Größte Stadt Südamerikas"
            ),
            GeoLocation(
                id = "buenos_aires",
                name = "Buenos Aires",
                country = "Argentinien",
                region = Region.SOUTH_AMERICA,
                latitude = -34.6037,
                longitude = -58.3816,
                hint = "Tango"
            ),
            GeoLocation(
                id = "santiago",
                name = "Santiago",
                country = "Chile",
                region = Region.SOUTH_AMERICA,
                latitude = -33.4489,
                longitude = -70.6693,
                hint = "Anden"
            ),
            GeoLocation(
                id = "cusco",
                name = "Cusco",
                country = "Peru",
                region = Region.SOUTH_AMERICA,
                latitude = -13.5320,
                longitude = -71.9675,
                hint = "Schwarz-weiß gestreifte Pfosten"
            ),
            GeoLocation(
                id = "cairo",
                name = "Kairo",
                country = "Ägypten",
                region = Region.AFRICA,
                latitude = 30.0444,
                longitude = 31.2357,
                hint = "Pyramiden"
            ),
            GeoLocation(
                id = "cape_town",
                name = "Kapstadt",
                country = "Südafrika",
                region = Region.AFRICA,
                latitude = -33.9249,
                longitude = 18.4241,
                hint = "Tafelberg"
            ),
            GeoLocation(
                id = "nairobi",
                name = "Nairobi",
                country = "Kenia",
                region = Region.AFRICA,
                latitude = -1.2921,
                longitude = 36.8219,
                hint = "Safari"
            ),
            GeoLocation(
                id = "tokyo",
                name = "Tokio",
                country = "Japan",
                region = Region.ASIA,
                latitude = 35.6762,
                longitude = 139.6503,
                hint = "Shibuya Crossing"
            ),
            GeoLocation(
                id = "osaka",
                name = "Osaka",
                country = "Japan",
                region = Region.ASIA,
                latitude = 34.6937,
                longitude = 135.5023,
                hint = "Takoyaki"
            ),
            GeoLocation(
                id = "seoul",
                name = "Seoul",
                country = "Südkorea",
                region = Region.ASIA,
                latitude = 37.5665,
                longitude = 126.9780,
                hint = "K-Pop"
            ),
            GeoLocation(
                id = "beijing",
                name = "Peking",
                country = "China",
                region = Region.ASIA,
                latitude = 39.9042,
                longitude = 116.4074,
                hint = "Verbotene Stadt"
            ),
            GeoLocation(
                id = "shanghai",
                name = "Shanghai",
                country = "China",
                region = Region.ASIA,
                latitude = 31.2304,
                longitude = 121.4737,
                hint = "Bund-Promenade"
            ),
            GeoLocation(
                id = "bangkok",
                name = "Bangkok",
                country = "Thailand",
                region = Region.ASIA,
                latitude = 13.7563,
                longitude = 100.5018,
                hint = "Street Food"
            ),
            GeoLocation(
                id = "singapore",
                name = "Singapur",
                country = "Singapur",
                region = Region.ASIA,
                latitude = 1.3521,
                longitude = 103.8198,
                hint = "Marina Bay Sands"
            ),
            GeoLocation(
                id = "sydney",
                name = "Sydney",
                country = "Australien",
                region = Region.OCEANIA,
                latitude = -33.8688,
                longitude = 151.2093,
                hint = "Opernhaus"
            ),
            GeoLocation(
                id = "melbourne",
                name = "Melbourne",
                country = "Australien",
                region = Region.OCEANIA,
                latitude = -37.8136,
                longitude = 144.9631,
                hint = "Australisches Kulturzentrum"
            ),
            GeoLocation(
                id = "auckland",
                name = "Auckland",
                country = "Neuseeland",
                region = Region.OCEANIA,
                latitude = -36.8509,
                longitude = 174.7645,
                hint = "City of Sails"
            )
        )
    }
}
