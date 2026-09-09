package alic.malorodow.geoguessr_app.data.location

import alic.malorodow.geoguessr_app.domain.model.GeoLocation
import alic.malorodow.geoguessr_app.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Lokale, statische Implementierung des [LocationRepository]-Interfaces.
 *
 * Diese Klasse gehört zum Data Layer der Clean Architecture: Sie stellt
 * die konkrete Umsetzung bereit, wie Standortdaten tatsächlich
 * beschafft werden. Die eigentlichen Rohdaten sind in
 * [GeoLocationDataSource] ausgelagert, um Datenhaltung und
 * Zugriffslogik klar zu trennen (Single-Responsibility-Prinzip).
 *
 * Der leere Konstruktor mit @Inject signalisiert Hilt, dass diese
 * Klasse ohne weitere Abhängigkeiten instanziiert werden kann. Über
 * RepositoryModule (siehe di/) wird sie an das Interface
 * [LocationRepository] gebunden.
 *
 * Der aktuelle, statische Standort-Datenbestand könnte als Erweiterung
 * durch offene Geodaten-Datensätze angereichert werden, wobei Land- und
 * Regionszuordnung einmalig offline über eine Reverse-Geocoding-Bibliothek vorberechnet
 * und in das bestehende GeoLocation-Format überführt würden, ohne dass sich das
 * [LocationRepository]-Interface oder aufrufende Klassen wie [GetRandomLocationsUseCase]
 * ändern müssten.
 */
class LocalLocationRepository @Inject constructor() : LocationRepository {

    /**
     * Liefert sämtliche verfügbaren Standorte der Anwendung.
     */
    override suspend fun getLocations(): List<GeoLocation> {
        return GeoLocationDataSource.ALL_LOCATIONS
    }

    /**
     * Liefert nur die Standorte, deren [GeoLocation.id] in der
     * übergebenen Liste [ids] enthalten ist – in exakt derselben
     * Reihenfolge wie [ids].
     *
     * WICHTIG: Die Reihenfolge von [ids] wird bewusst beibehalten,
     * indem über ids selbst iteriert wird (statt über den kompletten
     * Datenbestand zu filtern). Grund: Im Multiplayer-Modus definiert
     * die Reihenfolge der IDs die Rundenreihenfolge (Runde 1 = erste
     * ID, Runde 2 = zweite ID usw., siehe MatchSession.locationIds).
     * Eine simple filter()-Implementierung würde stattdessen die
     * ursprüngliche Reihenfolge von ALL_LOCATIONS zurückgeben, was zu
     * einer falschen Rundenzuordnung bei allen Mitspielern führen
     * könnte.
     */
    override suspend fun getLocationsByIds(ids: List<String>): List<GeoLocation> {
        val locationsById = GeoLocationDataSource.ALL_LOCATIONS.associateBy { it.id }
        return ids.mapNotNull { locationsById[it] }
    }
}