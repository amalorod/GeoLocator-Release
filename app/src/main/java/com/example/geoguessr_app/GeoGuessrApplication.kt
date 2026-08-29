package com.example.geoguessr_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Zentraler Einstiegspunkt der Anwendung für das Dependency-Injection-Framework Hilt.
 *
 * Die Annotation [HiltAndroidApp] löst zur Kompilierzeit die Codegenerierung
 * des anwendungsweiten Dependency-Graphen aus. Dabei wird eine Basisklasse
 * (Hilt_GeoGuessrApplication) erzeugt, von der diese Klasse implizit erbt.
 * Dieser Graph bildet die Wurzel-Komponente, aus der sich alle weiteren,
 * kleineren Komponenten (z. B. ActivityComponent, ViewModelComponent)
 * ableiten. Ohne diese Klasse könnten @AndroidEntryPoint (Activities) und
 * @HiltViewModel (ViewModels) an keiner Stelle der App Abhängigkeiten
 * injiziert bekommen.
 *
 * Aktuell übernimmt diese Klasse ausschließlich die DI-Initialisierung.
 * Sie eignet sich jedoch grundsätzlich auch für globale App-weite
 * Initialisierungslogik (z. B. Logging-Setup, Crash-Reporting), die beim
 * Start der App unabhängig von einer konkreten Activity erfolgen muss.
 */
@HiltAndroidApp
class GeoGuessrApplication : Application()