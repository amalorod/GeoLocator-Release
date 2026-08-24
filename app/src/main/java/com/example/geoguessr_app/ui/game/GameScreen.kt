package com.example.geoguessr_app.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.ui.components.AppTopBar
import com.example.geoguessr_app.ui.components.HintPanel
import com.example.geoguessr_app.ui.components.PauseOverlay
import com.example.geoguessr_app.ui.multiplayer.MultiplayerGameViewModel
import com.example.geoguessr_app.ui.multiplayer.MultiplayerScoreboard
import com.example.geoguessr_app.ui.multiplayer.SessionUiState
import com.example.geoguessr_app.ui.multiplayer.SessionViewModel
import com.example.geoguessr_app.ui.statistics.MatchSummaryDialog
import com.example.geoguessr_app.ui.theme.AppThemeMode
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.StreetViewPanoramaView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import java.util.Locale

private val EUROPE_CENTER = LatLng(54.0, 15.0)
private const val INITIAL_ZOOM = 3.5f
private const val STREET_VIEW_SEARCH_RADIUS_METERS = 500


/**
 * Verbindet das Hilt-ViewModel mit der zustandslosen Oberfläche.
 */
@Composable
fun GameRoute(
    currentThemeName: String,
    currentTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    onHomeClick: () -> Unit,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        GameScreen(
            uiState = uiState,
            sessionUiState = sessionUiState,
            currentThemeName = currentThemeName,
            currentTheme = currentTheme,
            onThemeSelected = onThemeSelected,
            onGuessSelected = viewModel::selectGuess,
            onShowGuessMap = viewModel::showGuessMap,
            onShowStreetView = viewModel::showStreetView,
            onHomeClick = onHomeClick,
            onPauseGame = viewModel::pauseGame,
            onSubmitGuess = viewModel::submitGuess,
            onNextRound = viewModel::startNextRound,
            onDismissQuest = viewModel::dismissCompletedQuest,
            onExitGame = onExitGame,
            onRetryLoading = viewModel::retryLoading,
            modifier = Modifier.fillMaxSize()
        )

        val gameStatistics = uiState.gameStatistics
        if (uiState.isGameFinished && gameStatistics != null) {
            MatchSummaryDialog(
                statistics = gameStatistics,
                onDetailsClick = {
                    // kommt im nächsten Schritt
                },
                onNewGameClick = {
                    viewModel.startNewGame(
                        uiState.gameMode
                    )
                },
                onHomeClick = onHomeClick
            )
        }

        val currentHint = uiState.currentLocation?.hint

        if (
            currentHint != null &&
            !uiState.isLoading &&
            !uiState.isRoundFinished &&
            !uiState.isGameFinished
        ) {
            HintPanel(
                hint = currentHint,
                roundNumber = uiState.currentRound,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }

        if (uiState.waitingForPlayers) {
            WaitingForPlayersOverlay()
        }

        if (uiState.isPaused) {
            PauseOverlay(
                onResume = viewModel::resumeGame
            )
        }
    }
}

@Composable
fun MultiplayerGameRoute(
    sessionId: String,
    currentThemeName: String,
    currentTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    onHomeClick: () -> Unit,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MultiplayerGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionId) {
        viewModel.loadSessionLocations(sessionId)
        sessionViewModel.observeSession(sessionId)
    }

    Box(modifier = modifier.fillMaxSize()) {
        GameScreen(
            uiState = uiState,
            sessionUiState = sessionUiState,
            currentThemeName = currentThemeName,
            currentTheme = currentTheme,
            onThemeSelected = onThemeSelected,
            onGuessSelected = viewModel::selectGuess,
            onShowGuessMap = viewModel::showGuessMap,
            onShowStreetView = viewModel::showStreetView,
            onHomeClick = onHomeClick,
            onPauseGame = { /* Pause im MP evtl. anders */ },
            onSubmitGuess = viewModel::submitGuess,
            onNextRound = viewModel::startNextRound,
            onDismissQuest = viewModel::dismissCompletedQuest,
            onExitGame = {
                viewModel.leaveGame()
                onExitGame()
            },
            onRetryLoading = { viewModel.loadSessionLocations(sessionId) },
            modifier = Modifier.fillMaxSize()
        )
    }
}


/**
 * Zustandslose Darstellung des vollständigen Spielbildschirms.
 */
@Composable
private fun GameScreen(
    uiState: GameUiState,
    sessionUiState: SessionUiState,
    currentThemeName: String,
    currentTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    onGuessSelected: (GeoCoordinate) -> Unit,
    onShowGuessMap: () -> Unit,
    onShowStreetView: () -> Unit,
    onHomeClick: () -> Unit,
    onPauseGame: () -> Unit,
    onSubmitGuess: () -> Unit,
    onNextRound: () -> Unit,
    onExitGame: () -> Unit,
    onRetryLoading: () -> Unit,
    onDismissQuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quest Completion Modal
        if (uiState.newlyCompletedQuest != null) {
            AlertDialog(
                onDismissRequest = onDismissQuest,
                title = { Text("✅ Quest erfüllt!") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.newlyCompletedQuest.icon, fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(uiState.newlyCompletedQuest.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(uiState.newlyCompletedQuest.description, textAlign = TextAlign.Center)
                    }
                },
                confirmButton = {
                    Button(onClick = onDismissQuest) {
                        Text("Super!")
                    }
                }
            )
        }

        AppTopBar(
            currentThemeName = currentThemeName,
            currentTheme = currentTheme,
            onThemeSelected = onThemeSelected,
            onHomeClick = onHomeClick,
            onPauseClick = onPauseGame,
            isPauseEnabled = !uiState.isLoading && !uiState.isRoundFinished && !uiState.isGameFinished && !uiState.isPaused,
        )

        GameStatusHeader(
            score = uiState.totalScore,
            currentRound = uiState.currentRound,
            totalRounds = uiState.totalRounds,
            remainingSeconds = uiState.remainingSeconds,
            gameModeName = uiState.gameMode.displayName,

        )

        if (uiState.isMultiplayer) {
            MultiplayerScoreboard(
                players = sessionUiState.players,
                modifier = Modifier.fillMaxWidth()
            )
        }



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

            uiState.viewMode == GameViewMode.STREET_VIEW -> {
                val currentLocation = uiState.currentLocation

                if (currentLocation != null) {
                    GameStreetView(
                        location = currentLocation,
                        isUserNavigationEnabled = uiState.gameMode.streetViewNavigationEnabled,
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

        val actualLocation = uiState.currentLocation
        val guessedLocation = uiState.guessedLocation

        if (actualLocation != null && guessedLocation != null) {
            RoundResultMap(
                actualLocation = actualLocation,
                guessedLocation = guessedLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    // Karte nimmt den gesamten verfügbaren Ergebnisbereich an
                    .weight(1f)
            )
        }

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

        if (uiState.isMultiplayer) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "⏳ Warte auf Mitspieler...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            Button(
                onClick = onNextRound,
                modifier = Modifier.padding(top = 16.dp)
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
    isUserNavigationEnabled: Boolean,
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
            panorama.isUserNavigationEnabled = isUserNavigationEnabled
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
        position = CameraPosition.fromLatLngZoom(EUROPE_CENTER, INITIAL_ZOOM)
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

@Composable
private fun RoundResultMap(
    actualLocation: GeoLocation,
    guessedLocation: GeoCoordinate,
    modifier: Modifier = Modifier
) {
    val actualPosition = LatLng(
        actualLocation.latitude,
        actualLocation.longitude
    )

    val guessedPosition = LatLng(
        guessedLocation.latitude,
        guessedLocation.longitude
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            EUROPE_CENTER,
            INITIAL_ZOOM
        )
    }

    var isMapLoaded by remember {
        mutableStateOf(false)
    }

    val bounds = remember(
        actualPosition,
        guessedPosition
    ) {
        LatLngBounds.Builder()
            .include(actualPosition)
            .include(guessedPosition)
            .build()
    }

    val boundsPaddingPixels = with(LocalDensity.current) {
        48.dp.roundToPx()
    }

    LaunchedEffect(isMapLoaded, bounds) {
        if (isMapLoaded) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    boundsPaddingPixels
                ),
                durationMs = 800
            )
        }
    }

    GoogleMap(
        onMapLoaded = {
            isMapLoaded = true
        },
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = rememberUpdatedMarkerState(actualPosition),
            title = "Tatsächlicher Standort"
        )

        Marker(
            state = rememberUpdatedMarkerState(guessedPosition),
            title = "Dein Tipp"
        )

        Polyline(
            points = listOf(
                actualPosition,
                guessedPosition
            ),
            color = Color.Red,
            width = 8f,
            geodesic = true
        )
    }
}



    @Composable
    private fun GameStatusHeader(
        score: Int,
        currentRound: Int,
        totalRounds: Int,
        remainingSeconds: Int,
        gameModeName: String
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 14.dp,
                            vertical = 7.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Score: $score")
                    Text("Runde: $currentRound/$totalRounds")
                    Text("$remainingSeconds Sek.")
                }
            }

            Surface(
                color = Color(0xFFD4AF37),
                contentColor = Color(0xFF2B2100),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = gameModeName,
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 3.dp
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }



/**
 * Overlay, das angezeigt wird, wenn man auf andere Spieler wartet.
 */
@Composable
fun WaitingForPlayersOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Warten auf Spieler...")
            }
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
