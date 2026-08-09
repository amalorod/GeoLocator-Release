package com.example.geoguessr_app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CalculateScoreUseCaseTest {

    private val calculateScore = CalculateScoreUseCase()

    @Test
    fun exactGuessReturnsMaximumScore() {
        val result = calculateScore(distanceKilometers = 0.0)

        assertEquals(5_000, result)
    }

    @Test
    fun distanceOfTwoThousandKilometersReturnsReducedScore() {
        val result = calculateScore(distanceKilometers = 2_000.0)

        assertEquals(1_839, result)
    }

    @Test
    fun negativeDistanceThrowsException() {
        assertThrows(IllegalArgumentException::class.java) {
            calculateScore(distanceKilometers = -1.0)
        }
    }
}
