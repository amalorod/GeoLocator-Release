package com.example.geoguessr_app.domain.model.custom

/**
 * Repräsentiert die geografischen Regionen, in die alle spielbaren
 * Standorte ([GeoLocation]) eingeteilt werden können.
 *
 * Als Enum-Klasse modelliert, da die Menge möglicher Regionen fest und
 * zur Kompilierzeit bekannt ist – im Gegensatz zu einer offenen Liste
 * von Strings verhindert das ungültige oder inkonsistent geschriebene
 * Regionsbezeichnungen im gesamten Projekt.
 *
 * Wird insbesondere im individuellen Spielmodus verwendet, um den
 * verfügbaren Standortpool auf eine bestimmte Region einzuschränken
 * (siehe Doku, Kapitel 5.3 „Individueller Modus“), sowie potenziell für
 * länder-/regionenspezifische Spielmodi.
 *
 * @property displayName Für Nutzer lesbarer, deutscher Anzeigename der
 *   Region (z. B. in Auswahlmenüs des individuellen Modus), getrennt
 *   vom technischen Enum-Namen, um UI-Texte unabhängig vom Code pflegen
 *   zu können.
 */
enum class Region(val displayName: String) {
    EUROPE("Europa"),
    NORTH_AMERICA("Nordamerika"),
    SOUTH_AMERICA("Südamerika"),
    AFRICA("Afrika"),
    ASIA("Asien"),
    OCEANIA("Ozeanien"),

    /** Sonderfall: keine regionale Einschränkung, alle Standorte weltweit. */
    WORLD("Weltweit")
}