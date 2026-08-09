package com.example.geoguessr_app.domain.usecase

import com.example.geoguessr_app.domain.model.GeoCoordinate
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Berechnet die Luftlinienentfernung zwischen zwei Koordinaten.
 *
 * Verwendet wird die Haversine-Formel. Die Erde wird dabei vereinfacht
 * als Kugel mit einem mittleren Radius von 6.371 Kilometern betrachtet.
 *
 * Das Ergebnis wird in Kilometern zurückgegeben.
 */
class CalculateDistanceUseCase @Inject constructor() {

    operator fun invoke(
        actualLocation: GeoCoordinate,
        guessedLocation: GeoCoordinate
    ): Double {
        val latitudeDifference = Math.toRadians(
            guessedLocation.latitude - actualLocation.latitude
        )

        val longitudeDifference = Math.toRadians(
            guessedLocation.longitude - actualLocation.longitude
        )

        val actualLatitudeRadians =
            Math.toRadians(actualLocation.latitude)

        val guessedLatitudeRadians =
            Math.toRadians(guessedLocation.latitude)

        val haversineValue =
            sin(latitudeDifference / 2).let { it * it } +
                    cos(actualLatitudeRadians) *
                    cos(guessedLatitudeRadians) *
                    sin(longitudeDifference / 2).let { it * it }

        val centralAngle = 2 * atan2(
            sqrt(haversineValue),
            sqrt(1 - haversineValue)
        )

        return EARTH_RADIUS_KILOMETERS * centralAngle
    }

    private companion object {
        const val EARTH_RADIUS_KILOMETERS = 6_371.0
    }
}