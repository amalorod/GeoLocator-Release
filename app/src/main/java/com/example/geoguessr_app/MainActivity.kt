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

/**
 * Einziger Activity-Einstiegspunkt der Anwendung.
 *
 * Die App verwendet eine Single-Activity-Architektur. Sämtliche Screens
 * werden mit Jetpack Compose dargestellt und über den NavHost gewechselt.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            var themeMode by rememberSaveable {
                mutableStateOf(AppThemeMode.LIGHT)
            }
            GeoGuessr_AppTheme(themeMode = themeMode) {

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GeoGuessrNavHost(
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
