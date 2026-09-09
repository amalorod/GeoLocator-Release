package alic.malorodow.geoguessr_app.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/**
 * Zentrale, prozessweit einmalige DataStore-Instanz für die gemeinsam
 * genutzte Preferences-Datei "geoguessr_preferences".
 *
 * Als Kotlin-Extension-Property auf [Context] deklariert und über den
 * `preferencesDataStore`-Property-Delegate erzeugt. Dieses Delegate
 * garantiert, dass pro Prozess und Dateiname genau eine [androidx.datastore.core.DataStore]-
 * Instanz existiert (Singleton-Verhalten) – DataStore verbietet es,
 * mehrere Instanzen für dieselbe Datei gleichzeitig zu öffnen, da dies
 * zu einer IllegalStateException zur Laufzeit führen würde.
 *
 * Wird von mehreren Repository-Klassen (z. B. [DailyQuestDataStoreRepository],
 * StatisticsDataStoreRepository) über `context.dataStore` gemeinsam
 * genutzt, damit alle auf dieselbe Datei zugreifen.
 *
 * Muss laut Android-Dokumentation als Top-Level-Property in genau einer
 * Datei deklariert werden, nicht mehrfach im Projekt.
 */
val Context.dataStore by preferencesDataStore(
    name = "geoguessr_preferences"
)