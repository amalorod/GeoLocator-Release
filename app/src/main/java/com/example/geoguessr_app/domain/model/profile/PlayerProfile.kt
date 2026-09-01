package com.example.geoguessr_app.domain.model.profile

import com.google.firebase.database.PropertyName

/**
 * Repräsentiert das Benutzerprofil eines Spielers (siehe Doku, Kapitel
 * 4.5 „Profile Screen“).
 *
 * ARCHITEKTUR-HINWEIS: Im Gegensatz zu den übrigen, technologiefreien
 * Domain-Modellen (z. B. [GeoLocation], [GeoCoordinate]) importiert
 * diese Klasse mit [PropertyName] direkt eine Firebase-spezifische
 * Annotation. Damit verletzt sie streng genommen das Prinzip der Clean
 * Architecture, wonach der Domain-Layer unabhängig von konkreten
 * Frameworks bleiben soll. Der pragmatische Grund: Firebase Realtime
 * Database mappt Datenbankfelder standardmäßig über den Property-Namen;
 * die Annotationen stellen sicher, dass das Mapping auch bei
 * abweichender Namenskonvention zuverlässig funktioniert. Eine strengere
 * Trennung wäre möglich, indem ein separates Data-Transfer-Object (DTO)
 * im Data-Layer für die Firebase-Serialisierung verwendet und erst beim
 * Verlassen des Data-Layers in dieses reine Domain-Modell überführt
 * wird.
 *
 * Alle Properties sind als var deklariert (statt val wie in den übrigen
 * Modellen), da der Firebase-Realtime-Database-Deserializer die
 * Property-Werte nach der Objekterzeugung per Reflection setzt und
 * dafür veränderliche Felder benötigt. Aus demselben Grund besitzt jede
 * Property einen Standardwert, damit ein parameterloser Konstruktor
 * existiert.
 *
 * @property playerId Eindeutige, von Firebase Authentication vergebene
 *   Benutzer-ID.
 * @property playerName Vom Nutzer gewählter Anzeigename (siehe Doku,
 *   Profilerstellung beim ersten Aufruf des Profile Screens).
 * @property profileImageUrl URL des in Firebase Storage abgelegten
 *   Profilbilds; null, falls noch kein Bild hochgeladen wurde.
 * @property createdAt Zeitstempel der Profilerstellung in Millisekunden
 *   seit Unix-Epoch, dient u. a. der Anzeige des Registrierungsdatums.
 */
data class PlayerProfile(
    @get:PropertyName("playerId")
    @set:PropertyName("playerId")
    var playerId: String = "",

    @get:PropertyName("playerName")
    @set:PropertyName("playerName")
    var playerName: String = "Spieler",

    @get:PropertyName("profileImageUrl")
    @set:PropertyName("profileImageUrl")
    var profileImageUrl: String? = null,

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Long = 0L,

    // Nicht in Firebase gespeichert und bewusst ohne @PropertyName, da der
    // Gast-Status rein lokal initialisiert wird. Default true, damit ein frisch erzeugtes
    // PlayerProfile() (siehe GeoGuessrNavHost, Fallback für den Gast-Fall)
    // automatisch als Gast gilt, ohne dass jeder Aufrufer isGuest separat
    // setzen müsste.
    var isGuest: Boolean = true
)