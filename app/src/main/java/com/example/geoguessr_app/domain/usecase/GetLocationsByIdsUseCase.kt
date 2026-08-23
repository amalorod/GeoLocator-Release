package com.example.geoguessr_app.domain.usecase

import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.repository.LocationRepository
import javax.inject.Inject

class GetLocationsByIdsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(ids: List<String>): List<GeoLocation> {
        return locationRepository.getLocationsByIds(ids)
    }
}