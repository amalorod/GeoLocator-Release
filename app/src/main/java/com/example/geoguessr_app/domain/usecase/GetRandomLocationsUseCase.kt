package com.example.geoguessr_app.domain.usecase

import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.model.custom.Region
import com.example.geoguessr_app.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Wählt eine gewünschte Anzahl unterschiedlicher Zufallsstandorte aus.
 */
class GetRandomLocationsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {

    suspend operator fun invoke(
        count: Int,
        region: Region = Region.WORLD
    ): List<GeoLocation> {
        require(count > 0) {
            "Die Anzahl der Standorte muss größer als null sein."
        }

        val allLocations = locationRepository.getLocations()
        val filteredLocations = if (region == Region.WORLD) {
            allLocations
        } else {
            allLocations.filter { it.region == region }
        }

        require(filteredLocations.size >= count) {
            "Für $count Runden sind nicht genügend Standorte in der Region ${region.displayName} vorhanden."
        }

        return filteredLocations.shuffled().take(count)
    }
}