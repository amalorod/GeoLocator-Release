package com.example.geoguessr_app.domain.usecase

import com.example.geoguessr_app.domain.model.GeoCoordinate
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import javax.inject.Inject

/**
 * Berechnet die Luftlinienentfernung zwischen zwei Koordinaten.
 *
 * Verwendet wird die Haversine-Formel, ein Standardverfahren zur
 * Distanzberechnung auf einer Kugeloberfläche. Sie berücksichtigt die
 * Erdkrümmung und liefert dadurch deutlich genauere Ergebnisse als eine
 * simple euklidische Distanzberechnung auf Breiten-/Längengraden, die
 * bei größeren Entfernungen stark verzerren würde. Die Erde wird dabei
 * vereinfacht als perfekte Kugel mit einem mittleren Radius von 6.371
 * Kilometern betrachtet (statt als exaktes Ellipsoid), was für die
 * Zwecke dieses Spiels eine ausreichend präzise Näherung darstellt.
 *
 * Als eigenständiger UseCase im Domain-Layer modelliert, statt die
 * Berechnung direkt im ViewModel durchzuführen. Dadurch lässt sich die
 * Distanzberechnung isoliert per Unit-Test überprüfen (siehe
 * androidTest/test-Verzeichnis) und unabhängig von der restlichen
 * Spiellogik wiederverwenden – z. B. sowohl im Einzelspieler- als auch
 * im Multiplayer-Modus.
 *
 * Die Klasse implementiert die operator fun invoke()-Konvention, wodurch
 * eine Instanz wie eine Funktion aufgerufen werden kann
 * (calculateDistanceUseCase(a, b) statt
 * calculateDistanceUseCase.execute(a, b)). Das ist ein in
 * UseCase-getriebenen Architekturen gängiges Kotlin-Idiom und macht
 * Aufrufstellen im Code kompakter und lesbarer.
 *
 * Das Ergebnis wird in Kilometern zurückgegeben.
 */
class CalculateDistanceUseCase @Inject constructor() {

    operator fun invoke(
        actualLocation: GeoCoordinate,
        guessedLocation: GeoCoordinate
    ): Double {
        // Differenz der Breitengrade, umgerechnet von Grad in Radiant,
        // da alle trigonometrischen Funktionen in Kotlin Radiant
        // erwarten statt Grad.
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

        // Haversine-Term: kombiniert die Differenz der Breitengrade mit
        // dem Produkt der Kosinuswerte beider Breitengrade und der
        // Differenz der Längengrade. Der Wertebereich liegt zwischen 0
        // (identische Position) und 1 (maximale Entfernung).
        val haversineValue =
            sin(latitudeDifference / 2).let { it * it } +
                    cos(actualLatitudeRadians) *
                    cos(guessedLatitudeRadians) *
                    sin(longitudeDifference / 2).let { it * it }

        // Zentralwinkel zwischen beiden Punkten auf der Kugeloberfläche,
        // berechnet über atan2 statt arcsin, da atan2 numerisch
        // stabiler ist und auch für nahezu antipodale Punkte (180 Grad
        // Entfernung) korrekt funktioniert.
        val centralAngle = 2 * atan2(
            sqrt(haversineValue),
            sqrt(1 - haversineValue)
        )

        // Bogenlänge = Radius * Zentralwinkel, liefert die tatsächliche
        // Entfernung entlang der Erdoberfläche in Kilometern.
        return EARTH_RADIUS_KILOMETERS * centralAngle
    }

    private companion object {
        const val EARTH_RADIUS_KILOMETERS = 6_371.0
    }
}