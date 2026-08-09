package com.example.geoguessr_app.domain.usecase

import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Wählt eine gewünschte Anzahl unterschiedlicher Zufallsstandorte aus.
 *
 * Die Auswahl ist Geschäftslogik und gehört deshalb nicht in den
 * Compose-Screen oder das Repository.
 */
class GetRandomLocationsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {

    suspend operator fun invoke(count: Int): List<GeoLocation> {
        require(count > 0) {
            "Die Anzahl der Standorte muss größer als null sein."
        }

        val locations = locationRepository.getLocations()

        require(locations.size >= count) {
            "Für $count Runden sind nicht genügend Standorte vorhanden."
        }

        return locations.shuffled().take(count)
    }
}