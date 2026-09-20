package alic.malorodow.geoguessr_app.domain.usecase

import alic.malorodow.geoguessr_app.domain.model.GeoCoordinate
import alic.malorodow.geoguessr_app.domain.model.GeoLocation
import alic.malorodow.geoguessr_app.domain.model.custom.Region
import alic.malorodow.geoguessr_app.domain.repository.LocationRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetRandomLocationsUseCaseTest {

    private class FakeLocationRepository(
        private val locations: List<GeoLocation>
    ) : LocationRepository {
        override suspend fun getLocations(): List<GeoLocation> = locations
        override suspend fun getLocationsByIds(ids: List<String>): List<GeoLocation> {
            val byId = locations.associateBy { it.id }
            return ids.mapNotNull { byId[it] }
        }
    }

    private val europeLocations = listOf(
        GeoLocation("munich", "München", "Deutschland", Region.EUROPE, GeoCoordinate(48.1, 11.5), "Hint"),
        GeoLocation("hamburg", "Hamburg", "Deutschland", Region.EUROPE, GeoCoordinate(53.5, 9.9), "Hint"),
        GeoLocation("lisbon", "Lissabon", "Portugal", Region.EUROPE, GeoCoordinate(38.7, -9.1), "Hint"),
        GeoLocation("athens", "Athen", "Griechenland", Region.EUROPE, GeoCoordinate(37.9, 23.7), "Hint"),
        GeoLocation("budapest", "Budapest", "Ungarn", Region.EUROPE, GeoCoordinate(47.4, 19.0), "Hint")
    )

    private lateinit var fakeRepository: FakeLocationRepository
    private lateinit var getRandomLocations: GetRandomLocationsUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeLocationRepository(europeLocations)
        getRandomLocations = GetRandomLocationsUseCase(fakeRepository)
    }

    @Test
    fun testLocationsAreDrawnWithoutReplacementUntilPoolExhausted() = runBlocking {
        // Erste Ziehung: 3 Standorte
        val batch1 = getRandomLocations(count = 3, region = Region.EUROPE)
        assertEquals(3, batch1.size)
        assertEquals(3, batch1.map { it.id }.toSet().size)

        // Zweite Ziehung: 2 Standorte
        val batch2 = getRandomLocations(count = 2, region = Region.EUROPE)
        assertEquals(2, batch2.size)

        // Überprüfen, dass über beide Ziehungen hinweg alle 5 Standorte eindeutig gezogen wurden
        val combinedIds = (batch1 + batch2).map { it.id }
        assertEquals(5, combinedIds.toSet().size)
    }

    @Test
    fun testCycleResetsAfterPoolExhausted() = runBlocking {
        // Ziehe alle 5 Standorte
        val batch1 = getRandomLocations(count = 5, region = Region.EUROPE)
        assertEquals(5, batch1.size)

        // Nach Ausführen von 5 Standorten ist der Pool voll ausgenutzt.
        // Nächster Aufruf muss einen neuen Zyklus auslösen und wieder einen Standort liefern.
        val batch2 = getRandomLocations(count = 1, region = Region.EUROPE)
        assertEquals(1, batch2.size)
        assertTrue(europeLocations.any { it.id == batch2.first().id })
    }

    @Test
    fun testNoImmediateDuplicateOnCycleBoundary() = runBlocking {
        val repoTwoLocations = FakeLocationRepository(
            listOf(
                GeoLocation("loc1", "Ort 1", "Land 1", Region.EUROPE, GeoCoordinate(0.0, 0.0), "H1"),
                GeoLocation("loc2", "Ort 2", "Land 2", Region.EUROPE, GeoCoordinate(1.0, 1.0), "H2")
            )
        )
        val useCase = GetRandomLocationsUseCase(repoTwoLocations)

        val batch1 = useCase(count = 2, region = Region.EUROPE)
        val lastOfBatch1 = batch1.last()

        val batch2 = useCase(count = 2, region = Region.EUROPE)
        val firstOfBatch2 = batch2.first()

        assertNotEquals(
            "Der erste Ort im neuen Zyklus sollte nicht direkt der letzte Ort des alten Zyklus sein",
            lastOfBatch1.id,
            firstOfBatch2.id
        )
    }

    @Test
    fun testResetHistoryClearsUsedLocations() = runBlocking {
        val batch1 = getRandomLocations(count = 3, region = Region.EUROPE)
        assertEquals(3, batch1.size)

        getRandomLocations.resetHistory(Region.EUROPE)

        // Nach dem Reset stehen wieder alle 5 Orte zur Verfügung
        val batch2 = getRandomLocations(count = 5, region = Region.EUROPE)
        assertEquals(5, batch2.size)
    }
}
