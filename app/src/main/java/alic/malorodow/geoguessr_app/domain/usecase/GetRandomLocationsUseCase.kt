package alic.malorodow.geoguessr_app.domain.usecase

import alic.malorodow.geoguessr_app.domain.model.GeoLocation
import alic.malorodow.geoguessr_app.domain.model.custom.CustomGameSettings
import alic.malorodow.geoguessr_app.domain.model.custom.Region
import alic.malorodow.geoguessr_app.domain.repository.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wählt eine gewünschte Anzahl unterschiedlicher Zufallsstandorte aus,
 * optional eingeschränkt auf eine bestimmte [Region].
 *
 * Speichert pro App-Session den Verlauf der bereits genutzten Standorte per Region,
 * um eine gleichmäßige Verteilung ("Bag System" / "Deck Shuffling") zu gewährleisten:
 * Jeder Standort einer Region kommt genau einmal an die Reihe, bevor sich ein Standort
 * wiederholen kann.
 *
 * Dient als zentrale Anlaufstelle für die Standortauswahl im klassischen
 * Einzelspieler-Modus (ohne Regionsfilter, da region einen Standardwert von Region.WORLD besitzt)
 * sowie dem individuellen Modus, in dem der Nutzer über [CustomGameSettings.region]
 * gezielt eine Region vorgeben kann.
 */
@Singleton
class GetRandomLocationsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {

    /**
     * Speichert bereits verwendete Standort-IDs pro Region für die laufende App-Session,
     * um vorzeitige Wiederholungen zu vermeiden.
     */
    private val usedLocationIdsByRegion = mutableMapOf<Region, MutableSet<String>>()

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
        require(count > 0) {
            "Die Anzahl der Standorte muss größer als null sein."
        }

        val allLocations = locationRepository.getLocations()

        val filteredLocations = if (region == Region.WORLD) {
            allLocations
        } else {
            allLocations.filter { loc -> loc.region == region }
        }

        require(filteredLocations.isNotEmpty()) {
            "Für die Region ${region.displayName} sind keine Standorte vorhanden."
        }

        val usedIds = usedLocationIdsByRegion.getOrPut(region) { mutableSetOf() }
        val result = mutableListOf<GeoLocation>()

        while (result.size < count) {
            // Unbenutzte Standorte in der aktuellen Region ermitteln
            var unusedLocations = filteredLocations.filter { it.id !in usedIds }

            // Falls alle Standorte der Region bereits einmal vorkamen,
            // wird die Historie für diese Region zurückgesetzt (neuer Zyklus).
            if (unusedLocations.isEmpty()) {
                usedIds.clear()
                unusedLocations = filteredLocations.toList()
            }

            var shuffledBlock = unusedLocations.shuffled()

            // Verhindert direkte Wiederholungen desselben Orts beim Zyklusübergang,
            // sofern mehr als ein Standort in der Region verfügbar ist.
            if (result.isNotEmpty() && filteredLocations.size > 1 && shuffledBlock.first() == result.last()) {
                val mutableBlock = shuffledBlock.toMutableList()
                while (mutableBlock.first() == result.last()) {
                    mutableBlock.shuffle()
                }
                shuffledBlock = mutableBlock
            }

            val needed = count - result.size
            val toTake = shuffledBlock.take(needed)

            result.addAll(toTake)
            toTake.forEach { usedIds.add(it.id) }
        }

        return result
    }

    /**
     * Setzt die Historie der gezogenen Standorte zurück (z. B. für Tests oder manuelle Resets).
     */
    fun resetHistory(region: Region? = null) {
        if (region != null) {
            usedLocationIdsByRegion[region]?.clear()
        } else {
            usedLocationIdsByRegion.clear()
        }
    }
}