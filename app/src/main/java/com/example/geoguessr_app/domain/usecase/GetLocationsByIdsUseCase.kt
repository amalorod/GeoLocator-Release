package com.example.geoguessr_app.domain.usecase

import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Ruft gezielt die Standorte ab, deren IDs in einer vorgegebenen Liste
 * enthalten sind.
 *
 * Wird insbesondere im Multiplayer-Modus benötigt: Damit alle Spieler
 * exakt dieselbe Partie mit denselben Standorten spielen (siehe Doku,
 * Kapitel 3.4 „Spielsitzungen“), werden die Standort-IDs einmalig beim
 * Erstellen der [MatchSession] festgelegt und über
 * [MatchSession.locationIds] an alle Geräte synchronisiert. Jedes Gerät
 * lädt anschließend über diesen UseCase dieselben, konkreten
 * [GeoLocation]-Objekte anhand ihrer IDs, statt selbst eine
 * unabhängige Zufallsauswahl zu treffen.
 *
 * Die Klasse greift dabei nicht direkt auf eine konkrete
 * Repository-Implementierung zu, sondern ausschließlich auf das
 * [LocationRepository]-Interface. Dadurch bleibt der UseCase unabhängig
 * davon, ob die Standorte lokal ([LocalLocationRepository]) oder
 * zukünftig über eine Remote-Quelle geladen werden.
 */
class GetLocationsByIdsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(ids: List<String>): List<GeoLocation> {
        return locationRepository.getLocationsByIds(ids)
    }
}