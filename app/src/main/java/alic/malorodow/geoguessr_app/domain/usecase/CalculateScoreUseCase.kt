package alic.malorodow.geoguessr_app.domain.usecase

import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.roundToInt

/**
 * Berechnet die Rundenpunktzahl anhand der Entfernung zwischen
 * geschätztem und tatsächlichem Standort (siehe Doku, Kapitel 1
 * „Kernfunktionen“: „Die Punktzahl basiert auf der Genauigkeit der
 * Schätzung“).
 *
 * Die Berechnung erfolgt über eine exponentielle Abklingfunktion
 * (Score = MAXIMUM_SCORE * e^(-distanz / DISTANCE_FACTOR)).
 *
 * Diese Wahl gegenüber einer linearen Abnahme hat einen spielerischen Vorteil:
 * Bereits kleine Verbesserungen bei sehr genauen Tipps (z. B. von 5 km
 * auf 1 km Entfernung) wirken sich stärker auf die Punktzahl aus als
 * dieselbe Verbesserung bei bereits großen Entfernungen (z. B. von
 * 3.000 km auf 2.996 km). Ein exakter Treffer (Entfernung 0) ergibt
 * dabei exp(0) = 1, also die volle Punktzahl von 5.000.
 *
 * Wie [CalculateDistanceUseCase] als eigenständiger, per operator
 * invoke() aufrufbarer UseCase modelliert, um die Punkteberechnung
 * unabhängig testbar und wiederverwendbar zu halten (sowohl im
 * Einzelspieler- als auch im Multiplayer-Scoreboard verwendet).
 */
class CalculateScoreUseCase @Inject constructor() {

    operator fun invoke(distanceKilometers: Double): Int {
        // Fail-Fast-Prüfung: Eine negative Entfernung wäre fachlich
        // unsinnig und deutet auf einen Fehler in der aufrufenden
        // Berechnung (z. B. CalculateDistanceUseCase) hin.
        require(distanceKilometers >= 0.0) {
            "Die Entfernung darf nicht negativ sein."
        }

        val calculatedScore =
            MAXIMUM_SCORE * exp(-distanceKilometers / DISTANCE_FACTOR)

        // roundToInt() rundet den kontinuierlichen Exponentialwert auf
        // eine ganzzahlige Punktzahl. coerceIn() begrenzt das Ergebnis
        // zusätzlich auf den gültigen Bereich [0, MAXIMUM_SCORE] als
        // Absicherung gegen Rundungsartefakte bei extrem kleinen
        // beziehungsweise theoretisch negativen Zwischenwerten.
        return calculatedScore
            .roundToInt()
            .coerceIn(0, MAXIMUM_SCORE)
    }

    private companion object {
        // Maximal erreichbare Punktzahl bei einem exakten Treffer.
        const val MAXIMUM_SCORE = 5_000

        // Steuert, wie schnell die Punktzahl mit zunehmender Entfernung
        // abfällt. Ein größerer Wert würde die Kurve "flacher" machen
        // (großzügigere Bewertung auch bei größeren Entfernungen), ein
        // kleinerer Wert würde die Bewertung strenger machen.
        const val DISTANCE_FACTOR = 2_000.0
    }
}