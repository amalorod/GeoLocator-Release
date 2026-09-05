// data/location/GeoLocationDataSource.kt
package com.example.geoguessr_app.data.location

import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.model.custom.Region

/**
 * Statischer Datenbestand aller im Spiel verfügbaren Standorte.
 *
 * Ausgelagert aus LocalLocationRepository, um Datenhaltung (diese
 * Datei) und Zugriffslogik (LocalLocationRepository) klar zu trennen
 * (Single-Responsibility-Prinzip). Jeder Eintrag kombiniert die
 * geografische Position mit Metadaten wie Land, Region und einem
 * textuellen Hinweis für das Hint-System (siehe Doku, Kapitel 5.5).
 */
object GeoLocationDataSource {
    val ALL_LOCATIONS = listOf(
        GeoLocation(
            id = "berlin",
            name = "Berlin",
            country = "Deutschland",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(52.5200, 13.4050),
            hint = "Der Ort befindet sich Nordöstlich Deutschlands"
        ),
        GeoLocation(
            id = "paris",
            name = "Paris",
            country = "Frankreich",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(48.8566, 2.3522),
            hint = "Croissants, Kaffee, Eifelturm"
        ),
        GeoLocation(
            id = "rome",
            name = "Rom",
            country = "Italien",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(41.9028, 12.4964),
            hint = "Eine damalige Weltmacht"
        ),
        GeoLocation(
            id = "london",
            name = "London",
            country = "Vereinigtes Königreich",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(51.5074, -0.1278),
            hint = "Queen __i_____h"
        ),
        GeoLocation(
            id = "madrid",
            name = "Madrid",
            country = "Spanien",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(40.4168, -3.7038),
            hint = "Urlaub, Fußball, Real ______"
        ),
        GeoLocation(
            id = "vienna",
            name = "Wien",
            country = "Österreich",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(48.2082, 16.3738),
            hint = "Dieses Schnitzel kennt jeder"
        ),
        GeoLocation(
            id = "prague",
            name = "Prag",
            country = "Tschechien",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(50.0755, 14.4378),
            hint = "Vepřo-knedlo-zelo"
        ),
        GeoLocation(
            id = "amsterdam",
            name = "Amsterdam",
            country = "Niederlande",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(52.3676, 4.9041),
            hint = "Fahrräder, Fahrräder, Fahrräder"
        ),
        GeoLocation(
            id = "new_york",
            name = "New York",
            country = "USA",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(40.7128, -74.0060),
            hint = "City Hall Park"
        ),
        GeoLocation(
            id = "new_york2",
            name = "New York",
            country = "USA",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(40.68875236646943, -74.04378912021065),
            hint = "American Cookies"
        ),
        GeoLocation(
            id = "los_angeles",
            name = "Los Angeles",
            country = "USA",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(34.13096949056064, -118.32409766915146),
            hint = "Hollywood"
        ),
        GeoLocation(
            id = "los_angeles2",
            name = "Los Angeles",
            country = "USA",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(34.00652426934249, -118.49497301929661),
            hint = "WestCoaster"
        ),
        GeoLocation(
            id = "navajo_nation_reservation",
            name = "Arizona",
            country = "USA",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(36.729610972306446, -110.10733801194735),
            hint = "Unendliche Straßen"
        ),
        GeoLocation(
            id = "chicago",
            name = "Chicago",
            country = "USA",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(41.8781, -87.6298),
            hint = "Windy City"
        ),
        GeoLocation(
            id = "toronto",
            name = "Toronto",
            country = "Kanada",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(43.642567, -79.387054),
            hint = "CN Tower"
        ),
        GeoLocation(
            id = "philadelphia",
            name = "Philadelphia",
            country = "America",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(39.93395503549924, -75.17742661972696),
            hint = "CN Tower"
        ),
        GeoLocation(
            id = "vancouver",
            name = "Vancouver",
            country = "Kanada",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(49.2827, -123.1207),
            hint = "Pazifikküste Kanadas"
        ),
        GeoLocation(
            id = "mexico_city",
            name = "Mexiko-Stadt",
            country = "Mexiko",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(19.4326, -99.1332),
            hint = "Azteken und Tacos"
        ),
        GeoLocation(
            id = "rio",
            name = "Rio de Janeiro",
            country = "Brasilien",
            region = Region.SOUTH_AMERICA,
            coordinate = GeoCoordinate(-22.9068, -43.1729),
            hint = "Christusstatue"
        ),
        GeoLocation(
            id = "sao_paulo",
            name = "São Paulo",
            country = "Brasilien",
            region = Region.SOUTH_AMERICA,
            coordinate = GeoCoordinate(-23.5505, -46.6333),
            hint = "Größte Stadt Südamerikas"
        ),
        GeoLocation(
            id = "buenos_aires",
            name = "Buenos Aires",
            country = "Argentinien",
            region = Region.SOUTH_AMERICA,
            coordinate = GeoCoordinate(-34.6037, -58.3816),
            hint = "Tango"
        ),
        GeoLocation(
            id = "santiago",
            name = "Santiago",
            country = "Chile",
            region = Region.SOUTH_AMERICA,
            coordinate = GeoCoordinate(-26.736308888215632, -70.73559911686097),
            hint = "Anden"
        ),
        GeoLocation(
            id = "cusco",
            name = "Cusco",
            country = "Peru",
            region = Region.SOUTH_AMERICA,
            coordinate = GeoCoordinate(-13.5320, -71.9675),
            hint = "Schwarz-weiß gestreifte Pfosten"
        ),
        GeoLocation(
            id = "cairo",
            name = "Kairo",
            country = "Ägypten",
            region = Region.AFRICA,
            coordinate = GeoCoordinate(30.0444, 31.2357),
            hint = "Pyramiden"
        ),
        GeoLocation(
            id = "cape_town",
            name = "Kapstadt",
            country = "Südafrika",
            region = Region.AFRICA,
            coordinate = GeoCoordinate(-34.354000, 18.489716),
            hint = "Cape Point"
        ),
        GeoLocation(
            id = "cape_town2",
            name = "Kapstadt",
            country = "Südafrika",
            region = Region.AFRICA,
            coordinate = GeoCoordinate(-33.949371744086605, 18.40541916314918),
            hint = "Tafelberg"
        ),
        GeoLocation(
            id = "nairobi",
            name = "Nairobi",
            country = "Kenia",
            region = Region.AFRICA,
            coordinate = GeoCoordinate(-1.2921, 36.8219),
            hint = "Safari"
        ),
        GeoLocation(
            id = "tokyo",
            name = "Tokio",
            country = "Japan",
            region = Region.ASIA,
            coordinate = GeoCoordinate(35.65947417051811, 139.7005350666233),
            hint = "Shibuya Crossing"
        ),
        GeoLocation(
            id = "osaka",
            name = "Osaka",
            country = "Japan",
            region = Region.ASIA,
            coordinate = GeoCoordinate(34.6930597, 135.4957532),
            hint = "Takoyaki"
        ),
        GeoLocation(
            id = "seoul",
            name = "Seoul",
            country = "Südkorea",
            region = Region.ASIA,
            coordinate = GeoCoordinate(37.5665, 126.9780),
            hint = "K-Pop"
        ),
        GeoLocation(
            id = "beijing",
            name = "Peking",
            country = "China",
            region = Region.ASIA,
            coordinate = GeoCoordinate(39.916389, 116.396393),
            hint = "Verbotene Stadt"
        ),
        GeoLocation(
            id = "shanghai",
            name = "Shanghai",
            country = "China",
            region = Region.ASIA,
            coordinate = GeoCoordinate(31.22887768691524, 121.48729952118009),
            hint = "New-Town"
        ),
        GeoLocation(
            id = "bangkok",
            name = "Bangkok",
            country = "Thailand",
            region = Region.ASIA,
            coordinate = GeoCoordinate(13.7563, 100.5018),
            hint = "Street Food"
        ),
        GeoLocation(
            id = "singapore",
            name = "Singapur",
            country = "Malaysia",
            region = Region.ASIA,
            coordinate = GeoCoordinate(1.2787931049130339, 103.85883089218818),
            hint = "Marina Bay Sands"
        ),
        GeoLocation(
            id = "sydney",
            name = "Sydney",
            country = "Australien",
            region = Region.OCEANIA,
            coordinate = GeoCoordinate(-33.8688, 151.2093),
            hint = "Opernhaus"
        ),
        GeoLocation(
            id = "melbourne",
            name = "Melbourne",
            country = "Australien",
            region = Region.OCEANIA,
            coordinate = GeoCoordinate(-37.815021608645516, 144.96638280288556),
            hint = "Australisches Kulturzentrum"
        ),
        GeoLocation(
            id = "auckland",
            name = "Auckland",
            country = "Neuseeland",
            region = Region.OCEANIA,
            coordinate = GeoCoordinate(-36.8509, 174.7645),
            hint = "City of Sails"
        )
    )
}