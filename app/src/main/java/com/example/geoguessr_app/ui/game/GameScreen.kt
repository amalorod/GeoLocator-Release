package com.example.geoguessr_app.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import java.util.Locale

// Konstanten für die Kartenkonfiguration
private val WORLD_CENTER = LatLng(0.0, 0.0)
private const val INITIAL_ZOOM = 1f

/**
 * Verbindet das Hilt-ViewModel mit dem zustandslosen GameScreen.
 */
@Composable
fun GameRoute(
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GameScreen(
        uiState = uiState,
        onGuessSelected = viewModel::selectGuess,
        onSubmitGuess = viewModel::submitGuess,
        onNextRound = viewModel::startNextRound,
        onExitGame = onExitGame,
        onRetryLoading = viewModel::retryLoading,
        modifier = modifier
    )
}

/**
 * Zustandslose Darstellung des Spielbildschirms.
 *
 * Der Screen zeigt ausschließlich den übergebenen Zustand und leitet
 * Benutzeraktionen über Callback-Funktionen an das ViewModel weiter.
 */
@Composable
private fun GameScreen(
    uiState: GameUiState,
    onGuessSelected: (GeoCoordinate) -> Unit,
    onSubmitGuess: () -> Unit,
    onNextRound: () -> Unit,
    onExitGame: () -> Unit,
    onRetryLoading: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Runde ${uiState.currentRound} von ${uiState.totalRounds}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Zeit: ${uiState.remainingSeconds} Sekunden",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Gesamtpunkte: ${uiState.totalScore}",
            style = MaterialTheme.typography.titleMedium
        )

        when {
            uiState.isLoading -> {
                Text("Standorte werden geladen …")
            }

            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Button(onClick = onRetryLoading) {
                    Text("Erneut versuchen")
                }
            }

            uiState.isGameFinished -> {
                GameResult(
                    totalScore = uiState.totalScore,
                    onExitGame = onExitGame,
                    modifier = Modifier.weight(1f)
                )
            }

            uiState.isRoundFinished -> {
                RoundResult(
                    uiState = uiState,
                    onNextRound = onNextRound,
                    modifier = Modifier.weight(1f)
                )
            }

            else -> {
                GuessMap(
                    selectedCoordinate = uiState.guessedLocation,
                    onGuessSelected = onGuessSelected,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = uiState.guessedLocation?.let {
                        formatCoordinates(it)
                    } ?: "Tippe auf die Karte, um einen Ort auszuwählen.",
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onSubmitGuess,
                    enabled = uiState.guessedLocation != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tipp abgeben")
                }
            }
        }

        if (!uiState.isGameFinished) {
            OutlinedButton(
                onClick = onExitGame,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Spiel verlassen")
            }
        }
    }
}

/**
 * Interaktive Weltkarte zur Auswahl des geschätzten Standorts.
 */
@Composable
private fun GuessMap(
    selectedCoordinate: GeoCoordinate?,
    onGuessSelected: (GeoCoordinate) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            WORLD_CENTER,
            INITIAL_ZOOM
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxWidth(),
        cameraPositionState = cameraPositionState,
        onMapClick = { selectedPosition ->
            onGuessSelected(
                GeoCoordinate(
                    latitude = selectedPosition.latitude,
                    longitude = selectedPosition.longitude
                )
            )
        }
    ) {
        selectedCoordinate?.let { coordinate ->
            Marker(
                state = rememberUpdatedMarkerState(
                    position = LatLng(
                        coordinate.latitude,
                        coordinate.longitude
                    )
                ),
                title = "Dein Tipp",
                snippet = formatCoordinates(coordinate)
            )
        }
    }
}

/**
 * Zeigt das Ergebnis einer abgeschlossenen Runde.
 */
@Composable
private fun RoundResult(
    uiState: GameUiState,
    onNextRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actualLocation = uiState.currentLocation
    val distance = uiState.roundDistanceKilometers
    val score = uiState.roundScore ?: 0

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        Text(
            text = "Runde abgeschlossen",
            style = MaterialTheme.typography.headlineSmall
        )

        if (actualLocation != null) {
            Text(
                text = "Gesuchter Ort: ${actualLocation.name}, " +
                        actualLocation.country,
                textAlign = TextAlign.Center
            )
        }

        if (distance != null) {
            Text("Entfernung: ${formatDistance(distance)} km")
        } else {
            Text("Kein Tipp innerhalb des Zeitlimits")
        }

        Text(
            text = "Rundenpunkte: $score",
            fontWeight = FontWeight.SemiBold
        )

        Button(
            onClick = onNextRound,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (uiState.currentRound == uiState.totalRounds) {
                    "Gesamtergebnis anzeigen"
                } else {
                    "Nächste Runde"
                }
            )
        }
    }
}

/**
 * Zeigt das Gesamtergebnis nach fünf Runden.
 */
@Composable
private fun GameResult(
    totalScore: Int,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        Text(
            text = "Spiel beendet!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Dein Endergebnis: $totalScore Punkte",
            style = MaterialTheme.typography.titleLarge
        )
        Button(
            onClick = onExitGame,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zum Hauptmenü")
        }
    }
}

// Hilfsfunktionen für die Textformatierung
private fun formatCoordinates(coordinate: GeoCoordinate): String {
    return String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", coordinate.latitude, coordinate.longitude)
}

private fun formatDistance(distance: Double): String {
    return String.format(Locale.getDefault(), "%.2f", distance)
}
