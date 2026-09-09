package alic.malorodow.geoguessr_app.data.datastore

import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Zentrale Definition aller DataStore-Schlüssel für die lokal persistierten
 * Lifetime-Statistiken eines Gast-Nutzers.
 *
 * Wird ausschließlich von [StatisticsDataStoreRepository] verwendet, um
 * Preferences-Werte typsicher zu lesen und zu schreiben. Die Auslagerung in
 * ein eigenes Objekt verhindert, dass Schlüssel-Strings z.B. "games_played"
 * redundant an mehreren Stellen im Code hartcodiert werden. Dadurch wird die
 * Single Source of Truth gewährleistet.
 *
 * Analog zu [DailyQuestPreferences] für den Daily-Quest-Bereich.
 */
object StatisticsPreferences {

    /** Anzahl der insgesamt gespielten Partien (Einzel- und Multiplayer). */
    val GAMES_PLAYED =
        intPreferencesKey("games_played")

    /** Anzahl aller bisher gespielten Runden über alle Partien hinweg. */
    val ROUNDS_PLAYED =
        intPreferencesKey("rounds_played")

    /** Aufsummierte Punktzahl aller jemals gespielten Partien. */
    val TOTAL_SCORE =
        intPreferencesKey("total_score")

    /** Höchste in einer einzelnen Partie erzielte Gesamtpunktzahl. */
    val BEST_GAME_SCORE =
        intPreferencesKey("best_game_score")

    /**
     * Aufsummierte Entfernung (in Kilometern) zwischen Tipp und tatsächlichem
     * Standort über alle gespielten Runden hinweg.
     */
    val TOTAL_DISTANCE =
        doublePreferencesKey("total_distance")

    /**
     * Serialisierte Liste der zuletzt gespielten Partien (JSON-String), aus
     * der [StatisticsDataStoreRepository] die Aktivitätshistorie für den
     * Statistics Screen rekonstruiert. Als String statt einer strukturierten
     * DataStore-Collection gespeichert, da Preferences DataStore selbst
     * keine verschachtelten Objektlisten unterstützt.
     */
    val RECENT_MATCHES =
        stringPreferencesKey("recent_matches")
}