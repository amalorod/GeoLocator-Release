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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.example.geoguessr_app.domain.model.GeoLocation
import com.google.android.gms.maps.StreetViewPanoramaView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import java.util.Locale

private val WORLD_CENTER = LatLng(0.0, 0.0)
private const val INITIAL_ZOOM = 1f
private const val STREET_VIEW_SEARCH_RADIUS_METERS = 500

/**
 * Verbindet das Hilt-ViewModel mit der zustandslosen Oberfläche.
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
        onShowGuessMap = viewModel::showGuessMap,
        onShowStreetView = viewModel::showStreetView,
        onSubmitGuess = viewModel::submitGuess,
        onNextRound = viewModel::startNextRound,
        onExitGame = onExitGame,
        onRetryLoading = viewModel::retryLoading,
        modifier = modifier
    )
}

/**
 * Zustandslose Darstellung des vollständigen Spielbildschirms.
 */
@Composable
private fun GameScreen(
    uiState: GameUiState,
    onGuessSelected: (GeoCoordinate) -> Unit,
    onShowGuessMap: () -> Unit,
    onShowStreetView: () -> Unit,
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

                Text(
                    if (uiState.currentRound == uiState.totalRounds) {
                        "Gesamtergebnis anzeigen"
                    } else {
                        "Nächste Runde"
                    }
                )
            }

            uiState.viewMode == GameViewMode.STREET_VIEW -> {
                val currentLocation = uiState.currentLocation

                if (currentLocation != null) {
                    GameStreetView(
                        location = currentLocation,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = onShowGuessMap,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tipp auf der Weltkarte abgeben")
                    }
                }
            }

            uiState.viewMode == GameViewMode.GUESS_MAP -> {
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

                OutlinedButton(
                    onClick = onShowStreetView,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zurück zu Street View")
                }
            }
        }
    }
}

/**
 * Zeigt das Ergebnis einer einzelnen Runde an.
 */
@Composable
private fun RoundResult(
    uiState: GameUiState,
    onNextRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Runde abgeschlossen!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Entfernung: ${uiState.roundDistanceKilometers?.let { "%.2f".format(Locale.US, it) } ?: "0"} km",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Punkte in dieser Runde: ${uiState.roundScore ?: 0}",
            style = MaterialTheme.typography.bodyLarge
        )

        uiState.currentLocation?.let { location ->
            Text(
                text = "Der Ort war: ${location.name}, ${location.country}",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = onNextRound,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Nächste Runde")
        }
    }
}

/**
 * Zeigt das Endergebnis des Spiels an.
 */
@Composable
private fun GameResult(
    totalScore: Int,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Spiel beendet!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Deine Gesamtpunktzahl: $totalScore",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Button(onClick = onExitGame) {
            Text("Zurück zum Hauptmenü")
        }
    }
}

/**
 * Zeigt die Google Street View Panorama-Ansicht.
 */
@Composable
private fun GameStreetView(
    location: GeoLocation,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val streetViewPanoramaView = remember {
        StreetViewPanoramaView(context)
    }

    // Lifecycle-Management für die StreetView-View
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> streetViewPanoramaView.onCreate(null)
                Lifecycle.Event.ON_START -> streetViewPanoramaView.onStart()
                Lifecycle.Event.ON_RESUME -> streetViewPanoramaView.onResume()
                Lifecycle.Event.ON_PAUSE -> streetViewPanoramaView.onPause()
                Lifecycle.Event.ON_STOP -> streetViewPanoramaView.onStop()
                Lifecycle.Event.ON_DESTROY -> streetViewPanoramaView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { streetViewPanoramaView },
        modifier = modifier.fillMaxSize()
    ) { view ->
        view.getStreetViewPanoramaAsync { panorama ->
            panorama.isPanningGesturesEnabled = true
            panorama.isZoomGesturesEnabled = true
            panorama.isUserNavigationEnabled = true
            panorama.isStreetNamesEnabled = false
            panorama.setPosition(
                LatLng(location.latitude, location.longitude),
                STREET_VIEW_SEARCH_RADIUS_METERS
            )
        }
    }
}

/**
 * Zeigt eine Weltkarte an, auf der der Nutzer seinen Tipp abgeben kann.
 */
@Composable
private fun GuessMap(
    selectedCoordinate: GeoCoordinate?,
    onGuessSelected: (GeoCoordinate) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(WORLD_CENTER, INITIAL_ZOOM)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            onGuessSelected(GeoCoordinate(latLng.latitude, latLng.longitude))
        }
    ) {
        selectedCoordinate?.let {
            Marker(
                state = rememberUpdatedMarkerState(LatLng(it.latitude, it.longitude)),
                title = "Dein Tipp"
            )
        }
    }
}

/**
 * Formatiert Koordinaten für die Anzeige.
 */
private fun formatCoordinates(coordinate: GeoCoordinate): String {
    return String.format(
        Locale.US,
        "Lat: %.4f, Lng: %.4f",
        coordinate.latitude,
        coordinate.longitude
    )
}
