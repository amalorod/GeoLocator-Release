package com.example.geoguessr_app.data.location

import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.model.custom.Region
import com.example.geoguessr_app.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Lokale Repository-Implementierung.
 */
/**
 * Lokale, statische Implementierung des [LocationRepository]-Interfaces.
 *
 * Diese Klasse gehört zum Data Layer der Clean Architecture: Sie stellt
 * die konkrete Umsetzung bereit, wie Standortdaten tatsächlich
 * beschafft werden – in diesem Fall aus einer fest im Code hinterlegten
 * Liste statt aus einer Datenbank oder einem Remote-Server. Domain- und
 * UI-Layer kennen ausschließlich das Interface [LocationRepository] und
 * bemerken daher nicht, dass die Daten lokal statt z. B. über Firebase
 * geladen werden. Diese Austauschbarkeit ist einer der zentralen
 * Vorteile der Clean Architecture.
 *
 * Der leere Konstruktor mit @Inject signalisiert Hilt, dass diese Klasse
 * ohne weitere Abhängigkeiten instanziiert werden kann. Über ein
 * RepositoryModule (siehe di/) wird sie an das Interface
 * [LocationRepository] gebunden, sodass ViewModels und UseCases die
 * Implementierung nie direkt referenzieren, sondern nur das Interface
 * injizieren.
 *
 * ERWEITERUNGSHORIZONT: Aktuell sind sämtliche ca. 30 Standorte direkt
 * als statische Liste im companion object dieser Klasse hinterlegt. Für
 * eine sauberere Trennung von Datenhaltung und Zugriffslogik könnten
 * die Rohdaten künftig in eine eigene Datei (z. B. eine separate
 * Datenquellen-Klasse) ausgelagert oder aus einer externen Quelle
 * (JSON-Datei, Remote-Datenbank) geladen werden, ohne dass sich das
 * öffentliche Interface dieser Klasse ändern müsste.
 */
class LocalLocationRepository @Inject constructor() : LocationRepository {

    /**
     * Liefert sämtliche verfügbaren Standorte der Anwendung.
     *
     * Als suspend-Funktion deklariert, obwohl der aktuelle Zugriff auf
     * eine In-Memory-Liste keine echte asynchrone Arbeit erfordert. Das
     * hält die Methode konsistent mit dem Interface [LocationRepository]
     * und ermöglicht einen späteren Wechsel auf eine tatsächlich
     * asynchrone Datenquelle (z. B. Netzwerk- oder Datenbankzugriff),
     * ohne dass aufrufender Code (UseCases, ViewModels) angepasst
     * werden müsste.
     */
    override suspend fun getLocations(): List<GeoLocation> {
        return ALL_LOCATIONS
    }

    /**
     * Liefert nur die Standorte, deren [GeoLocation.id] in der
     * übergebenen Liste [ids] enthalten ist.
     *
     * Wird u. a. im Multiplayer-Modus benötigt, damit alle Spieler
     * exakt dieselben, vorab festgelegten Standorte einer Session
     * erhalten (siehe Doku, Kapitel Spielsitzungen).
     */
    override suspend fun getLocationsByIds(ids: List<String>): List<GeoLocation> {
        return ALL_LOCATIONS.filter { it.id in ids }
    }

    companion object {
        /**
         * Statischer Datenbestand aller im Spiel verfügbaren Standorte.
         *
         * Jeder Eintrag kombiniert die geografische Position
         * ([GeoCoordinate]) mit Metadaten wie Land, Region und einem
         * textuellen Hinweis für das Hint-System (siehe Doku, Kapitel
         * 5.5). Die Region-Zuordnung ermöglicht die Filterung des
         * Standortpools im individuellen Spielmodus (siehe Doku,
         * Kapitel 5.3).
         */
        private val ALL_LOCATIONS = listOf(
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
                hint = "Freiheitsstatue und Wolkenkratzer"
            ),
            GeoLocation(
                id = "los_angeles",
                name = "Los Angeles",
                country = "USA",
                region = Region.NORTH_AMERICA,
                coordinate = GeoCoordinate(34.0522, -118.2437),
                hint = "Hollywood"
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
                coordinate = GeoCoordinate(43.6532, -79.3832),
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
                coordinate = GeoCoordinate(-33.4489, -70.6693),
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
                coordinate = GeoCoordinate(-33.9249, 18.4241),
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
                coordinate = GeoCoordinate(35.6762, 139.6503),
                hint = "Shibuya Crossing"
            ),
            GeoLocation(
                id = "osaka",
                name = "Osaka",
                country = "Japan",
                region = Region.ASIA,
                coordinate = GeoCoordinate(34.6937, 135.5023),
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
                coordinate = GeoCoordinate(39.9042, 116.4074),
                hint = "Verbotene Stadt"
            ),
            GeoLocation(
                id = "shanghai",
                name = "Shanghai",
                country = "China",
                region = Region.ASIA,
                coordinate = GeoCoordinate(31.2304, 121.4737),
                hint = "Bund-Promenade"
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
                country = "Singapur",
                region = Region.ASIA,
                coordinate = GeoCoordinate(1.3521, 103.8198),
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
                coordinate = GeoCoordinate(-37.8136, 144.9631),
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
}
