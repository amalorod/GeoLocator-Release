package com.example.geoguessr_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Zentraler Einstiegspunkt für Hilt.
 *
 * @HiltAndroidApp erzeugt den anwendungsweiten
 * Dependency-Injection-Container.
 */
@HiltAndroidApp
class GeoGuessrApplication : Application()