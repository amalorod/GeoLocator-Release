package com.example.geoguessr_app.domain.usecase

import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.model.custom.CustomGameSettings
import com.example.geoguessr_app.domain.model.custom.Region
import com.example.geoguessr_app.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Wählt eine gewünschte Anzahl unterschiedlicher Zufallsstandorte aus,
 * optional eingeschränkt auf eine bestimmte [Region].
 *
 * Dient als zentrale Anlaufstelle für die Standortauswahl im klassischen
 * Einzelspieler-Modus (ohne Regionsfilter,da region einen Standardwert von Region.WORLD besitzt)
 * sowie dem individuellen Modus, in dem der Nutzer über [CustomGameSettings.region]
 * gezielt eine Region vorgeben kann (siehe Doku, Kapitel 5.3 „Individueller Modus“).
 */
class GetRandomLocationsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {

    suspend operator fun invoke(
        count: Int, region: Region = Region.WORLD
    ): List<GeoLocation> {
        // Fail-Fast-Prüfung: Eine Anfrage nach null oder weniger
        // Standorten wäre fachlich unsinnig und deutet auf einen
        // Fehler in der aufrufenden Stelle hin (z. B. GameViewModel).
        require(count > 0) {
            "Die Anzahl der Standorte muss größer als null sein."
        }

        val allLocations = locationRepository.getLocations()

        // Region.WORLD bedeutet laut Definition in [Region] "keine
        // Einschränkung" – in diesem Fall wird der komplette
        // Standortpool verwendet, andernfalls wird auf die exakt
        // passende Region gefiltert.
        val filteredLocations = if (region == Region.WORLD) {
            allLocations
        } else {
            allLocations.filter { loc -> loc.region == region }
        }

        // Verhindert, dass shuffled().take(count) unbemerkt weniger
        // Standorte zurückgibt als angefordert, falls eine Region zu
        // wenige Einträge im Datenbestand besitzt. Ohne diese Prüfung
        // würde die Partie stillschweigend mit zu wenigen Runden
        // starten, statt den Nutzer auf das eigentliche Problem
        // (fehlende Standortdaten) hinzuweisen.
        require(filteredLocations.size >= count) {
            "Für $count Runden sind nicht genügend Standorte in der Region ${region.displayName} vorhanden."
        }

        // shuffled() liefert eine neue, zufällig durchmischte Liste;
        // take(count) entnimmt daraus die gewünschte Anzahl. Da
        // shuffled() keine Duplikate erzeugt, sind die zurückgegebenen
        // Standorte garantiert paarweise unterschiedlich.
        return filteredLocations.shuffled().take(count)
    }
}