package alic.malorodow.geoguessr_app.domain.model.custom

/**
 * Schwierigkeitsgrade für den individuellen Spielmodus.
 *
 * Analog zu [Region] als Enum modelliert, da die Menge der möglichen
 * Schwierigkeitsgrade fest definiert und zur Kompilierzeit bekannt ist.
 * Der konkrete Effekt jedes Schwierigkeitsgrads (z. B. reduzierte
 * Bewegungsfreiheit im Street View, begrenztes Zoomen, deaktivierte
 * Hinweise) wird nicht hier, sondern in der jeweiligen Spiellogik
 * (GameViewModel bzw. UseCases) ausgewertet. Dieses Enum liefert
 * lediglich die Auswahlmöglichkeit selbst.
 *
 * @property displayName Für Nutzer lesbarer, deutscher Anzeigename
 *   der im Auswahlmenü angezeigt wird.
 * @property initialLives Anzahl der Leben für diesen Schwierigkeitsgrad
 *   oder null bei unbegrenzten Leben.
 */
enum class CustomDifficulty(val displayName: String, val initialLives: Int? = null) {
    EASY("Leicht", initialLives = null),
    MEDIUM("Mittel", initialLives = 5),
    HARD("Schwer", initialLives = 3);

    /** Nur der Schwierigkeitsgrad Mittel/ Schwer hat eine begrenzte Anzahl an Leben,
     * die anderen Modi nicht*/
    val hasLimitedLives: Boolean get() = initialLives != null
}