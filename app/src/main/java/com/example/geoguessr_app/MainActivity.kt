package com.example.geoguessr_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.geoguessr_app.navigation.GeoGuessrNavHost
import com.example.geoguessr_app.ui.game.GameMode
import com.example.geoguessr_app.ui.splash.AppLogoSplashScreen
import com.example.geoguessr_app.ui.theme.GeoGuessr_AppTheme
import com.example.geoguessr_app.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint


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
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Theme-Einstellungen werden nicht mehr lokal in der Activity
            // gehalten, sondern über das app-weite ThemeViewModel aus dem
            // DataStore bezogen. Dadurch überlebt das gewählte Theme jetzt
            // auch App-Neustarts, nicht nur Konfigurationsänderungen.
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeSettings by themeViewModel.themeSettings.collectAsState()

            var selectedGameMode by rememberSaveable {
                mutableStateOf(GameMode.NORMAL)
            }

            // Zeigt nach dem kurzen System-Splash-Screen zusätzlich den
            // eigenen AppLogoSplashScreen mit Namensnennung, bevor der
            // eigentliche NavHost geladen wird (siehe AppLogoSplashScreen).
            var showAppSplash by rememberSaveable { mutableStateOf(true) }

            GeoGuessr_AppTheme(
                themeMode = themeSettings.mode, dynamicColor = themeSettings.dynamicColorEnabled
            ) {

                if (showAppSplash) {
                    AppLogoSplashScreen(onFinished = { showAppSplash = false })
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        GeoGuessrNavHost(
                            selectedGameMode = selectedGameMode,
                            onGameModeSelected = { mode -> selectedGameMode = mode },
                            onExitAppClick = { finishAffinity() },
                            modifier = Modifier.padding(innerPadding),
                            currentTheme = themeSettings.mode,
                            currentDynamicColorEnabled = themeSettings.dynamicColorEnabled,
                            onThemeSelected = { selectedTheme ->
                                themeViewModel.setThemeMode(selectedTheme)
                            },
                            onDynamicColorToggled = { enabled ->
                                themeViewModel.setDynamicColorEnabled(enabled)
                            },
                        )
                    }
                }
            }
        }
    }
}
