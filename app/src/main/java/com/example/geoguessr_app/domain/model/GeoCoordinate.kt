package com.example.geoguessr_app.domain.model

/**
 * Repräsentiert eine geografische Position in Dezimalgraden (WGS84).
 *
 * Diese Klasse ist ein reines Domain-Modell im Sinne der Clean
 * Architecture: Sie enthält ausschließlich Geschäftslogik (die
 * Gültigkeitsprüfung der Koordinaten) und keine Abhängigkeit zu
 * Android-, Compose- oder Google-Maps-APIs. Dadurch lässt sie sich ohne
 * Android-Kontext unit-testen und in mehreren Layern (Domain, Data, UI)
 * gleichermaßen verwenden.
 *
 * Wertebereiche nach WGS84-Standard:
 * - Breitengrad (latitude): -90.0 (Südpol) bis +90.0 (Nordpol)
 * - Längengrad (longitude): -180.0 bis +180.0 (Datumsgrenze)
 *
 * @property latitude Breitengrad in Dezimalgrad.
 * @property longitude Längengrad in Dezimalgrad.
 */
data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double
) {
    /**
     * Validiert die Koordinaten unmittelbar bei der Objekterzeugung
     * (Fail-Fast-Prinzip). Durch require() wird eine
     * IllegalArgumentException geworfen, sobald ungültige Werte
     * übergeben werden – dadurch können an keiner Stelle im Programm
     * fachlich unsinnige Koordinatenobjekte existieren, was nachfolgende
     * Berechnungen zuverlässiger macht.
     */
    init {
        require(latitude in -90.0..90.0) {
            "Der Breitengrad muss zwischen -90 und 90 liegen."
        }

        require(longitude in -180.0..180.0) {
            "Der Längengrad muss zwischen -180 und 180 liegen."
        }
    }
}