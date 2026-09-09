package alic.malorodow.geoguessr_app.data.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Definiert die typsicheren Preference-Schlüssel für die lokal
 * gespeicherten Profildaten eines Gast-Nutzers (Spieler-ID und
 * Anzeigename), die ohne Firebase-Account persistiert werden.
 *
 * Wie bei [DailyQuestPreferences] werden hierfür dedizierte
 * [stringPreferencesKey]-Objekte verwendet, um Tippfehler bei
 * String-Literalen zu vermeiden und die Typsicherheit beim Lesen/
 * Schreiben aus dem DataStore zu gewährleisten.
 */
object ProfilePreferences {

    /**
     * Eindeutige, lokal generierte Kennung des Gast-Spielers
     * (z. B. UUID), die den Nutzer ohne Login-Vorgang identifiziert.
     */
    val PLAYER_ID =
        stringPreferencesKey("player_id")

    /**
     * Vom Nutzer gewählter oder automatisch generierter Anzeigename,
     * der z. B. in Leaderboards oder auf dem Profilbildschirm angezeigt wird.
     */
    val PLAYER_NAME =
        stringPreferencesKey("player_name")
}