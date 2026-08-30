package com.example.geoguessr_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.geoguessr_app.navigation.GeoGuessrNavHost
import com.example.geoguessr_app.ui.theme.GeoGuessr_AppTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.geoguessr_app.ui.theme.AppThemeMode
import com.example.geoguessr_app.ui.game.GameMode


/**
 * Einziger Activity-Einstiegspunkt der Anwendung (Single-Activity-Architektur).
 *
 * Sämtliche Screens werden als Jetpack-Compose-Komposable dargestellt und
 * über den zentralen [GeoGuessrNavHost] gewechselt, statt über mehrere
 * Android-Activities. Das entspricht der modernen Android-Empfehlung und
 * vereinfacht die Navigation, da der gesamte Navigationsgraph innerhalb
 * einer einzigen Activity verwaltet wird.
 *
 * Die Annotation @AndroidEntryPoint markiert diese Activity als
 * Injektionspunkt für Hilt und ermöglicht es untergeordneten Composables,
 * über viewModel() automatisch mit @HiltViewModel-annotierten ViewModels
 * versorgt zu werden.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO (Architektur-Review): Statische Initialisierung umgehen Hilt.
        // Sollte langfristig durch Dependency Injection ersetzt werden
        // (siehe di/-Modul), um Konsistenz mit dem restlichen DI-Konzept
        // der App herzustellen.
        com.example.geoguessr_app.data.profile.ProfileRepository.initialize(this)
        com.example.geoguessr_app.data.statistics.StatisticsRepository.initialize(this)

        // Zeichnet den Content bis unter die System-Bars (Status-/Navigationsleiste),
        // damit die App ein modernes, randloses Erscheinungsbild erhält.
        enableEdgeToEdge()

        setContent {

            // App-weiter UI-State: Theme und Spielmodus werden hier gehalten,
            // da sie von mehreren, unabhängigen Screens innerhalb des
            // NavHosts gelesen bzw. verändert werden können. MainActivity
            // bildet damit den "lowest common ancestor" dieser beiden
            // States. rememberSaveable sorgt zusätzlich dafür, dass beide
            // Werte Konfigurationsänderungen (z. B. Bildschirmdrehung)
            // überleben.
            var themeMode by rememberSaveable {
                mutableStateOf(AppThemeMode.LIGHT)
            }
            var selectedGameMode by rememberSaveable {
                mutableStateOf(GameMode.NORMAL)
            }

            // Wendet das aktuell gewählte Farbschema global auf die
            // gesamte Compose-Hierarchie an.
            GeoGuessr_AppTheme(themeMode = themeMode) {

                // Scaffold liefert das Standard-Layout-Gerüst inkl.
                // sicherer Innenabstände (innerPadding), die an den
                // NavHost weitergegeben werden, damit Inhalte nicht von
                // System-UI-Elementen überlappt werden.
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GeoGuessrNavHost(
                        selectedGameMode = selectedGameMode,
                        onGameModeSelected = { mode -> selectedGameMode = mode },
                        // Beendet die gesamte Task (alle Activities der App),
                        // statt nur die aktuelle Activity zu schließen –
                        // relevant, da die App nur eine einzige Activity besitzt.
                        onExitAppClick = {
                            finishAffinity()
                        },
                        modifier = Modifier.padding(innerPadding),
                        currentThemeName = themeMode.displayName,
                        currentTheme = themeMode,
                        onThemeSelected = { selectedTheme ->
                            themeMode = selectedTheme
                        },
                    )
                }
            }
        }
    }
}
