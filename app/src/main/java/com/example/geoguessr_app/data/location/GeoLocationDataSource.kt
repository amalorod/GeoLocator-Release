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
        // EUROPE
        GeoLocation(
            id = "munich",
            name = "München",
            country = "Deutschland",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(48.137154, 11.576124),
            hint = "Oktoberfest"
        ),
        GeoLocation(
            id = "hamburg",
            name = "Hamburg",
            country = "Deutschland",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(53.551086, 9.993682),
            hint = "Elbphilharmonie"
        ),
        GeoLocation(
            id = "lisbon",
            name = "Lissabon",
            country = "Portugal",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(38.722252, -9.139337),
            hint = "Gelbe Straßenbahnen"
        ),
        GeoLocation(
            id = "athens",
            name = "Athen",
            country = "Griechenland",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(37.983810, 23.727539),
            hint = "Akropolis"
        ),
        GeoLocation(
            id = "budapest",
            name = "Budapest",
            country = "Ungarn",
            region = Region.EUROPE,
            coordinate = GeoCoordinate(47.497913, 19.040236),
            hint = "Donau"
        ),

// NORTH_AMERICA
        GeoLocation(
            id = "san_francisco",
            name = "San Francisco",
            country = "USA",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(37.774929, -122.419418),
            hint = "Golden Gate Bridge"
        ),
        GeoLocation(
            id = "las_vegas",
            name = "Las Vegas",
            country = "USA",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(36.169941, -115.139832),
            hint = "Kasinos"
        ),
        GeoLocation(
            id = "miami",
            name = "Miami",
            country = "USA",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(25.761681, -80.191788),
            hint = "Ocean Drive"
        ),
        GeoLocation(
            id = "montreal",
            name = "Montreal",
            country = "Kanada",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(45.501689, -73.567256),
            hint = "Französischsprachige Metropole"
        ),
        GeoLocation(
            id = "guadalajara",
            name = "Guadalajara",
            country = "Mexiko",
            region = Region.NORTH_AMERICA,
            coordinate = GeoCoordinate(20.659699, -103.349609),
            hint = "Mariachi"
        ),

// SOUTH_AMERICA
        GeoLocation(
            id = "lima",
            name = "Lima",
            country = "Peru",
            region = Region.SOUTH_AMERICA,
            coordinate = GeoCoordinate(-12.046374, -77.042793),
            hint = "Pazifikküste"
        ),
        GeoLocation(
            id = "medellin",
            name = "Medellín",
            country = "Kolumbien",
            region = Region.SOUTH_AMERICA,
            coordinate = GeoCoordinate(6.244203, -75.581215),
            hint = "Stadt des ewigen Frühlings"
        ),
        GeoLocation(
            id = "bogota",
            name = "Bogotá",
            country = "Kolumbien",
            region = Region.SOUTH_AMERICA,
            coordinate = GeoCoordinate(4.711000, -74.072090),
            hint = "Hochgelegene Hauptstadt"
        ),
        GeoLocation(
            id = "montevideo",
            name = "Montevideo",
            country = "Uruguay",
            region = Region.SOUTH_AMERICA,
            coordinate = GeoCoordinate(-34.901112, -56.164532),
            hint = "Río de la Plata"
        ),
        GeoLocation(
            id = "valparaiso",
            name = "Valparaíso",
            country = "Chile",
            region = Region.SOUTH_AMERICA,
            coordinate = GeoCoordinate(-33.047238, -71.612688),
            hint = "Bunte Hügel"
        ),

// AFRICA
        GeoLocation(
            id = "johannesburg",
            name = "Johannesburg",
            country = "Südafrika",
            region = Region.AFRICA,
            coordinate = GeoCoordinate(-26.204103, 28.047304),
            hint = "Größte Stadt Südafrikas"
        ),
        GeoLocation(
            id = "durban",
            name = "Durban",
            country = "Südafrika",
            region = Region.AFRICA,
            coordinate = GeoCoordinate(-29.858681, 31.021841),
            hint = "Indischer Ozean"
        ),
        GeoLocation(
            id = "pretoria",
            name = "Pretoria",
            country = "Südafrika",
            region = Region.AFRICA,
            coordinate = GeoCoordinate(-25.747868, 28.229271),
            hint = "Jacaranda-Bäume"
        ),
        GeoLocation(
            id = "mombasa",
            name = "Mombasa",
            country = "Kenia",
            region = Region.AFRICA,
            coordinate = GeoCoordinate(-4.043477, 39.668206),
            hint = "Hafenstadt"
        ),
        GeoLocation(
            id = "george",
            name = "George",
            country = "Südafrika",
            region = Region.AFRICA,
            coordinate = GeoCoordinate(-33.964806, 22.461842),
            hint = "Garden Route"
        ),

// ASIA
        GeoLocation(
            id = "kyoto",
            name = "Kyoto",
            country = "Japan",
            region = Region.ASIA,
            coordinate = GeoCoordinate(35.011636, 135.768029),
            hint = "Tempelstadt"
        ),
        GeoLocation(
            id = "yokohama",
            name = "Yokohama",
            country = "Japan",
            region = Region.ASIA,
            coordinate = GeoCoordinate(35.443707, 139.638031),
            hint = "Hafenstadt Japans"
        ),
        GeoLocation(
            id = "busan",
            name = "Busan",
            country = "Südkorea",
            region = Region.ASIA,
            coordinate = GeoCoordinate(35.179554, 129.075642),
            hint = "Küstenstadt"
        ),
        GeoLocation(
            id = "taipei",
            name = "Taipei",
            country = "Taiwan",
            region = Region.ASIA,
            coordinate = GeoCoordinate(25.033964, 121.564468),
            hint = "Taipei 101"
        ),
        GeoLocation(
            id = "hong_kong",
            name = "Hongkong",
            country = "China",
            region = Region.ASIA,
            coordinate = GeoCoordinate(22.319303, 114.169361),
            hint = "Victoria Harbour"
        ),

// OCEANIA
        GeoLocation(
            id = "brisbane",
            name = "Brisbane",
            country = "Australien",
            region = Region.OCEANIA,
            coordinate = GeoCoordinate(-27.469770, 153.025131),
            hint = "Queensland"
        ),
        GeoLocation(
            id = "perth",
            name = "Perth",
            country = "Australien",
            region = Region.OCEANIA,
            coordinate = GeoCoordinate(-31.950527, 115.860457),
            hint = "Westaustralien"
        ),
        GeoLocation(
            id = "adelaide",
            name = "Adelaide",
            country = "Australien",
            region = Region.OCEANIA,
            coordinate = GeoCoordinate(-34.928497, 138.600739),
            hint = "Südaustralien"
        ),
        GeoLocation(
            id = "wellington",
            name = "Wellington",
            country = "Neuseeland",
            region = Region.OCEANIA,
            coordinate = GeoCoordinate(-41.286461, 174.776230),
            hint = "Windige Hauptstadt"
        ),
        GeoLocation(
            id = "christchurch",
            name = "Christchurch",
            country = "Neuseeland",
            region = Region.OCEANIA,
            coordinate = GeoCoordinate(-43.532055, 172.636225),
            hint = "South Island"
        ),
    )
}