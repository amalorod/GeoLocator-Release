package com.example.geoguessr_app.domain.repository

import com.example.geoguessr_app.domain.model.GeoLocation

/**
 * Definiert, welche Standortdaten die Anwendung benötigt.
 *
 * Die Domain-Schicht kennt ausschließlich diese Schnittstelle und
 * nicht die konkrete Herkunft der Daten. Dadurch bleibt die
 * Spiellogik (UseCases wie [GetRandomLocationsUseCase] oder
 * [GetLocationsByIdsUseCase]) vollständig unabhängig davon, ob die
 * Standorte aktuell aus einer lokalen, statischen Liste
 * ([LocalLocationRepository]) oder künftig z. B. aus einer Firebase-
 * Datenbank oder einer Remote-API stammen. Dieses Prinzip der
 * Abhängigkeitsumkehr (Dependency Inversion) ist ein zentraler
 * Baustein der Clean Architecture (siehe Doku, Kapitel 2.1
 * „Architektur“): Der Domain-Layer hängt vom Interface ab, nicht von
 * einer konkreten Implementierung im Data-Layer.
 *
 * Über das RepositoryModule (siehe di/) wird zur Laufzeit festgelegt,
 * welche konkrete Klasse Hilt für dieses Interface bereitstellt.
 */
interface LocationRepository {

    /**
     * Liefert sämtliche im Spiel verfügbaren Standorte.
     *
     * Als suspend-Funktion deklariert, da eine spätere Implementierung
     * (z. B. ein Netzwerk- oder Datenbankzugriff) potenziell
     * asynchron ablaufen muss, auch wenn die aktuelle lokale
     * Implementierung keine echte asynchrone Arbeit durchführt.
     */
    suspend fun getLocations(): List<GeoLocation>

    /**
     * Liefert ausschließlich die Standorte, deren [GeoLocation.id] in
     * der übergebenen Liste [ids] enthalten ist.
     *
     * Wird insbesondere im Multiplayer-Modus benötigt, um anhand der
     * in einer [MatchSession] gespeicherten Standort-IDs auf jedem
     * Gerät dieselben Orte zu laden (siehe Doku, Kapitel 3.4
     * „Spielsitzungen“).
     */
    suspend fun getLocationsByIds(ids: List<String>): List<GeoLocation>
}