package alic.malorodow.geoguessr_app.domain.usecase

import alic.malorodow.geoguessr_app.domain.model.GeoLocation
import alic.malorodow.geoguessr_app.domain.model.custom.CustomGameSettings
import alic.malorodow.geoguessr_app.domain.model.custom.Region
import alic.malorodow.geoguessr_app.domain.repository.LocationRepository
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

    /**
     * Führt die zufällige Standortauswahl durch.
     *
     * @param count Die Anzahl der anzufordernden Standorte (muss > 0 sein).
     * @param region Die gewünschte geografische [Region] (Standard: [Region.WORLD]).
     * @return Eine Liste von zufällig durchmischten [GeoLocation]-Objekten der Länge [count].
     * @throws IllegalArgumentException wenn [count] <= 0 ist oder keine Standorte für die Region existieren.
     */
    suspend operator fun invoke(
        count: Int, region: Region = Region.WORLD
    ): List<GeoLocation> {
        // Fail-Fast-Prüfung: Eine Anfrage nach null oder weniger
        // Standorten wäre fachlich unsinnig und deutet auf einen
        // Fehler in der aufrufenden Stelle hin (z. B. GameViewModel).
        require(count > 0) {
            "Die Anzahl der Standorte muss größer als null sein."
        }

        // Lädt den gesamten Standortpool aus dem Repository.
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

        // Überprüft, ob überhaupt Standorte für die gewählte Region vorhanden sind.
        require(filteredLocations.isNotEmpty()) {
            "Für die Region ${region.displayName} sind keine Standorte vorhanden."
        }

        // Falls mehr Standorte angefordert wurden als in der Region existieren
        // (z. B. 20 angefordert für Modi ohne Rundenlimit), füllen wir die Liste
        // zyklisch mit erneut durchmischten Blöcken auf, sodass beliebig viele
        // Runden ohne Spielabbruch möglich sind.
        val result = mutableListOf<GeoLocation>()
        while (result.size < count) {
            var block = filteredLocations.shuffled()

            // Verhindert direkte Wiederholungen desselben Orts beim Blockübergang,
            // sofern mehr als ein Standort in der Region verfügbar ist.
            if (result.isNotEmpty() && filteredLocations.size > 1 && block.first() == result.last()) {
                val mutableBlock = block.toMutableList()
                while (mutableBlock.first() == result.last()) {
                    mutableBlock.shuffle()
                }
                block = mutableBlock
            }

            result.addAll(block)
        }

        // Schneidet genau die geforderte Anzahl an Standorten zu.
        return result.take(count)
    }
}