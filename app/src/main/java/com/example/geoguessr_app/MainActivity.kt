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
            GeoGuessr_AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GeoGuessrNavHost(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}