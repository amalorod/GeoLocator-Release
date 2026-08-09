package com.example.geoguessr_app.domain.usecase

import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.roundToInt

/**
 * Berechnet die Rundenzahl anhand der Entfernung.
 *
 * Ein exakter Treffer ergibt 5.000 Punkte. Mit zunehmender Entfernung
 * sinkt die Punktzahl exponentiell. Dadurch werden besonders genaue
 * Tipps stärker belohnt.
 */
class CalculateScoreUseCase @Inject constructor() {

    operator fun invoke(distanceKilometers: Double): Int {
        require(distanceKilometers >= 0.0) {
            "Die Entfernung darf nicht negativ sein."
        }

        val calculatedScore =
            MAXIMUM_SCORE * exp(-distanceKilometers / DISTANCE_FACTOR)

        return calculatedScore
            .roundToInt()
            .coerceIn(0, MAXIMUM_SCORE)
    }

    private companion object {
        const val MAXIMUM_SCORE = 5_000
        const val DISTANCE_FACTOR = 2_000.0
    }
}