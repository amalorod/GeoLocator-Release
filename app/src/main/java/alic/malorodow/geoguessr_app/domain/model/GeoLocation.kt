package alic.malorodow.geoguessr_app.domain.model

import androidx.compose.runtime.Immutable
import alic.malorodow.geoguessr_app.domain.model.custom.Region

/**
 * Repräsentiert einen spielbaren Standort innerhalb der Anwendung.
 *
 * Wie [GeoCoordinate] ist dieses Modell unabhängig gehalten:
 * Es enthält weder Android- noch Compose- oder Google Maps-spezifische
 * Typen. Dadurch bleibt die eigentliche Spiellogik (z. B. Auswahl eines
 * zufälligen Standorts, Punkteberechnung) vollständig von der konkreten
 * UI- oder Kartenimplementierung entkoppelt und ließe sich z. B. auch
 * ohne Google Maps testen oder durch einen anderen Kartendienst ersetzen.
 *
 * Die geografische Position wird über Komposition eingebunden
 * (val coordinate: GeoCoordinate) statt über einzelne latitude-/
 * longitude-Felder. Dadurch wird die Bereichsvalidierung aus
 * [GeoCoordinate] (siehe dortiger init-Block) automatisch für jeden
 * GeoLocation-Standort wiederverwendet, ohne sie hier erneut
 * implementieren zu müssen.
 *
 * @property id Eindeutiger Bezeichner des Standorts (z. B. für Firebase-
 *   Referenzen oder den Vergleich bereits gespielter Orte).
 * @property name Anzeigename des Ortes (z. B. Stadt oder Sehenswürdigkeit).
 * @property country Land, in dem sich der Standort befindet.
 * @property region Geografische Region (z. B. Europa), der der Standort
 *   zugeordnet ist – wird u. a. im individuellen Spielmodus zur
 *   Filterung des Standortpools verwendet.
 * @property coordinate Geografische Position des Standorts als
 *   validiertes [GeoCoordinate]-Wertobjekt.
 * @property hint Textueller Hinweis zum Standort, der Spielern im
 *   Hint-System zur Unterstützung angezeigt wird (z. B. bekannte
 *   Sehenswürdigkeiten oder kulturelle Merkmale).
 */
@Immutable
data class GeoLocation(
    val id: String,
    val name: String,
    val country: String,
    val region: Region,
    val coordinate: GeoCoordinate,
    val hint: String
) {
    /**
     * Komfort-Zugriffe auf Breiten- und Längengrad, damit aufrufender
     * Code (z. B. UI-Komponenten für Karten oder Street View) nicht bei
     * jedem Zugriff den Umweg über location.coordinate.latitude gehen
     * muss. Da es sich um berechnete get()-Properties ohne eigenes
     * Backing-Field handelt, wird kein zusätzlicher Speicher belegt und
     * es entsteht keine Dateninkonsistenz zwischen coordinate und
     * latitude/longitude.
     */
    val latitude: Double get() = coordinate.latitude
    val longitude: Double get() = coordinate.longitude
}