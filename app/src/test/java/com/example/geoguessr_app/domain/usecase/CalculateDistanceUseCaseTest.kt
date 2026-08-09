package com.example.geoguessr_app.domain.usecase

import com.example.geoguessr_app.domain.model.GeoCoordinate
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateDistanceUseCaseTest {

    private val calculateDistance = CalculateDistanceUseCase()

    @Test
    fun identicalCoordinatesReturnZeroKilometers() {
        val berlin = GeoCoordinate(
            latitude = 52.5200,
            longitude = 13.4050
        )

        val result = calculateDistance(
            actualLocation = berlin,
            guessedLocation = berlin
        )

        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun berlinAndParisReturnExpectedApproximateDistance() {
        val berlin = GeoCoordinate(
            latitude = 52.5200,
            longitude = 13.4050
        )

        val paris = GeoCoordinate(
            latitude = 48.8566,
            longitude = 2.3522
        )

        val result = calculateDistance(
            actualLocation = berlin,
            guessedLocation = paris
        )

        assertEquals(877.0, result, 2.0)
    }
}
