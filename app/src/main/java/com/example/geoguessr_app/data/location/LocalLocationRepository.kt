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
        return GeoLocationDataSource.ALL_LOCATIONS
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
        return GeoLocationDataSource.ALL_LOCATIONS.filter { it.id in ids }
    }

    
}
