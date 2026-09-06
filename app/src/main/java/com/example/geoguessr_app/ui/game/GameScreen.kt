package com.example.geoguessr_app.ui.game

import androidx.activity.compose.BackHandler
import com.example.geoguessr_app.ui.multiplayer.WaitingForPlayersOverlay
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geoguessr_app.domain.model.GeoCoordinate
import com.example.geoguessr_app.domain.model.GeoLocation
import com.example.geoguessr_app.ui.components.AppTopBar
import com.example.geoguessr_app.ui.components.HintPanel
import com.example.geoguessr_app.ui.components.PauseOverlay
import com.example.geoguessr_app.ui.multiplayer.MultiplayerGameViewModel
import com.example.geoguessr_app.ui.multiplayer.MultiplayerScoreboard
import com.example.geoguessr_app.ui.multiplayer.SessionUiState
import com.example.geoguessr_app.ui.multiplayer.SessionViewModel
import com.example.geoguessr_app.ui.components.MatchSummaryDialog
import com.example.geoguessr_app.ui.theme.AppThemeMode
import com.example.geoguessr_app.domain.statistics.GameStatistics
import com.example.geoguessr_app.ui.multiplayer.WaitingForPlayersOverlay
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
 * Bindet [GameViewModel] und [SessionViewModel] an die zustandslose
 * [GameScreen]-Oberfläche für den Einzelspieler- bzw. lokalen Spielablauf.
 *
 * Als "Route" fungiert diese Funktion als Adapter zwischen ViewModel-Layer
 * und UI-Layer (MVVM): Sie sammelt die StateFlows der ViewModels via
 * [collectAsStateWithLifecycle] – wichtig, da diese API im Gegensatz zu
 * [androidx.compose.runtime.collectAsState] das Sammeln automatisch pausiert,
 * wenn der Screen nicht im Vordergrund ist, und so unnötige Arbeit im
 * Hintergrund vermeidet.
 */
@Composable
fun GameRoute(
    currentTheme: AppThemeMode,
    currentDynamicColorEnabled: Boolean,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDynamicColorToggled: (Boolean) -> Unit,
    onHomeClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel
) {

    // Macht den System-Zurück-Button wirkungslos, damit der Nutzer eine laufende Runde nicht versehentlich
    // verlässt. Das reguläre Verlassen erfolgt stattdessen explizit über den onExitGame Button innerhalb von GameScreen.
    BackHandler(enabled = true) {}

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        GameScreen(
            uiState = uiState,
            // sessionUiState = SessionUiState() als fester Leerzustand statt
            // eines ungenutzten ViewModels – GameScreen benötigt ihn ohnehin
            // nur für uiState.isMultiplayer == true (siehe MultiplayerScoreboard).
            sessionUiState = SessionUiState(),
            currentTheme = currentTheme,
            currentDynamicColorEnabled = currentDynamicColorEnabled,
            onThemeSelected = onThemeSelected,
            onDynamicColorToggled = onDynamicColorToggled,
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
                statistics = gameStatistics, onDetailsClick = onStatisticsClick, onNewGameClick = {
                    viewModel.startNewGame(
                        uiState.gameMode
                    )
                }, onHomeClick = onHomeClick
            )
        }

        // Hinweis-Panel wird nur während einer aktiven, nicht abgeschlossenen
        // Runde eingeblendet – verhindert überlappende UI mit Rundenergebnis/Endscreen.
        val currentHint = uiState.currentLocation?.hint

        if (currentHint != null && !uiState.isLoading && !uiState.isRoundFinished && !uiState.isGameFinished) {
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

/**
 * Bindet [MultiplayerGameViewModel] und [SessionViewModel] an die gleiche
 * zustandslose [GameScreen]-Oberfläche für den Mehrspielermodus.
 *
 * Bewusst als separate Route statt Parametrisierung von [GameRoute], da sich
 * Datenquelle (Session-basiertes Laden via [sessionId]) und Spielende-Logik
 * (Sieger-/Verlierer-Ermittlung) fachlich klar vom Einzelspielerfall
 * unterscheiden – eine gemeinsame Route würde hier unnötig verzweigte
 * Bedingungslogik in eine einzige Funktion zwingen.
 */
@Composable
fun MultiplayerGameRoute(
    sessionId: String,
    currentTheme: AppThemeMode,
    currentDynamicColorEnabled: Boolean,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDynamicColorToggled: (Boolean) -> Unit,
    onHomeClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MultiplayerGameViewModel = hiltViewModel()
) {

    BackHandler(enabled = true) {
        // Bewusst leer: Zurückwischen während des Spiels wird ignoriert.

    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    // Lädt die Session-Standorte und startet die Session-Beobachtung genau
    // einmal, wenn eine (neue) sessionId vorliegt – nicht bei jeder Recomposition.
    LaunchedEffect(sessionId) {
        viewModel.loadSessionLocations(sessionId)
        sessionViewModel.observeSession(sessionId)
    }

    Box(modifier = modifier.fillMaxSize()) {
        GameScreen(
            uiState = uiState,
            sessionUiState = sessionUiState,
            currentTheme = currentTheme,
            currentDynamicColorEnabled = currentDynamicColorEnabled,
            onThemeSelected = onThemeSelected,
            onDynamicColorToggled = onDynamicColorToggled,
            onGuessSelected = viewModel::selectGuess,
            onShowGuessMap = viewModel::showGuessMap,
            onShowStreetView = viewModel::showStreetView,
            onHomeClick = onHomeClick,
            // AppTopBar zeigt den Pause-Button im Multiplayer nicht an.
            onPauseGame = { },
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

        if (uiState.isGameFinished) {
            val stats = GameStatistics(
                totalScore = uiState.totalScore, rounds = uiState.roundStatistics
            )
            MatchSummaryDialog(
                statistics = stats,
                isMultiplayer = true,
                isLocalPlayerWinner = uiState.isLocalPlayerWinner,
                winnerName = uiState.matchWinnerName,
                onDetailsClick = onStatisticsClick,
                onNewGameClick = onHomeClick,
                onHomeClick = onHomeClick
            )
        }
    }
}


/**
 * Zustandslose Darstellung des vollständigen Spielbildschirms.
 */
@Composable
private fun GameScreen(
    uiState: GameUiState,
    sessionUiState: SessionUiState,
    currentTheme: AppThemeMode,
    currentDynamicColorEnabled: Boolean,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDynamicColorToggled: (Boolean) -> Unit,
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


    var isStreetViewFullscreen by rememberSaveable { mutableStateOf(false) }
    // Im Vollbildmodus wird bewusst ausschließlich die Street-View-Ansicht
    // ohne Score-Header, Hint-Panel oder AppTopBar gerendert, damit die
    // Aufnahme tatsächlich den kompletten Bildschirm einnimmt. Ein early
    // return verhindert, dass die restliche Column-Struktur parallel
    // mitkomponiert wird (spart zusätzliche native StreetViewPanoramaView-
    // Instanzen und vermeidet Layout-Konflikte).

    // Die Gerätenavigation bleibt im GameScreen ausgeschalter, damit man nicht versehentlich beim umschauen in der StreetView-Ansicht
    // zurück zum Home-Bildschirm gelangt. Im FullScreen kann jedoch mittels zurück-Button des Geräts auf den GameScreen zurückgeschaltet werden.
    // Damit wird sichergestellt, dass der Nutzer nicht versehentlich den Spielbereich verlässt.
    BackHandler(enabled = true) {
        if (isStreetViewFullscreen) {
            isStreetViewFullscreen = false
        }

    }
    if (isStreetViewFullscreen && uiState.viewMode == GameViewMode.STREET_VIEW) {
        val currentLocation = uiState.currentLocation
        if (currentLocation != null) {
            Box(modifier = modifier.fillMaxSize()) {
                GameStreetView(
                    location = currentLocation,
                    isUserNavigationEnabled = uiState.isStreetViewNavigationEnabled,
                    modifier = Modifier.fillMaxSize()
                )
                FilledIconButton(
                    onClick = { isStreetViewFullscreen = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        // Nutzt die gewünschte Farbe aus dem aktuellen Farbschema
                        containerColor = MaterialTheme.colorScheme.primary,
                        // Bestimmt die Farbe des Icons (automatisch die passende Kontrastfarbe)
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Vollbild verlassen",
                        tint = Color.White
                    )
                }
            }
        }
        return
    }

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
                        Text(
                            uiState.newlyCompletedQuest.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(uiState.newlyCompletedQuest.description, textAlign = TextAlign.Center)
                    }
                },
                confirmButton = {
                    Button(onClick = onDismissQuest) {
                        Text("Super!")
                    }
                })
        }

        AppTopBar(
            currentTheme = currentTheme,
            currentDynamicColorEnabled = currentDynamicColorEnabled,
            onThemeSelected = onThemeSelected,
            onDynamicColorToggled = onDynamicColorToggled,
            onHomeClick = onHomeClick,
            showPauseButton = !uiState.isMultiplayer,
            onPauseGame = onPauseGame,
            onPauseClick = onPauseGame,
            isPauseEnabled = !uiState.isLoading && !uiState.isRoundFinished && !uiState.isGameFinished && !uiState.isPaused,
        )
        if (!uiState.isMultiplayer) {
            GameStatusHeader(
                score = uiState.totalScore,
                currentRound = uiState.currentRound,
                totalRounds = uiState.totalRounds,
                remainingSeconds = uiState.remainingSeconds,
                gameMode = uiState.gameMode,
                lives = uiState.lives
            )
        } else {
            GameStatusHeader(
                score = uiState.totalScore,
                currentRound = uiState.currentRound,
                totalRounds = uiState.totalRounds,
                remainingSeconds = uiState.remainingSeconds,
                gameMode = uiState.gameMode
            )
        }
        if (uiState.isMultiplayer) {
            MultiplayerScoreboard(
                players = sessionUiState.players,
                gameMode = uiState.gameMode,
                modifier = Modifier.fillMaxWidth()
            )
        }


        // Die Reihenfolge der Bedingungen wird mit Jetpack-Compose priorisiert: Lade- und
        // Fehlerzustände haben Vorrang vor Spielende/Rundenende, und erst danach
        // wird zwischen den eigentlichen Spielansichten (Street View / Guess Map)
        // unterschieden. So wird verhindert, dass z. B. während eines Ladevorgangs
        // gleichzeitig eine veraltete Street-View- oder Guess-Map-Ansicht aufblitzt.
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
                    uiState = uiState, onNextRound = onNextRound, modifier = Modifier.weight(1f)
                )
            }

            uiState.viewMode == GameViewMode.STREET_VIEW -> {
                val currentLocation = uiState.currentLocation

                if (currentLocation != null) {

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        GameStreetView(
                            location = currentLocation,
                            isUserNavigationEnabled = uiState.isStreetViewNavigationEnabled,
                            modifier = Modifier.fillMaxSize()
                        )


                        FilledIconButton(
                            onClick = { isStreetViewFullscreen = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                // Nutzt die gewünschte Farbe aus deinem aktuellen Farbschema
                                containerColor = MaterialTheme.colorScheme.primary,
                                // Bestimmt die Farbe des Icons (automatisch die passende Kontrastfarbe)
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Vollbild"
                            )
                        }
                    }

                    Button(
                        onClick = onShowGuessMap, modifier = Modifier.fillMaxWidth()
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

                Text(text = uiState.guessedLocation?.let {
                    formatCoordinates(it)
                } ?: "Tippe auf die Karte, um einen Ort auszuwählen.", textAlign = TextAlign.Center)

                Button(
                    onClick = onSubmitGuess,
                    enabled = uiState.guessedLocation != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tipp abgeben")
                }

                OutlinedButton(
                    onClick = onShowStreetView, modifier = Modifier.fillMaxWidth()
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
    uiState: GameUiState, onNextRound: () -> Unit, modifier: Modifier = Modifier
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

        // Hervorgehobene Box für Runden-Ergebnisse
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎯 Runde abgeschlossen!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Entfernung:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(text = uiState.roundDistanceKilometers?.let {
                        "%.2f km".format(Locale.US, it)
                    } ?: "0 km",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Punkte in dieser Runde:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "+${uiState.roundScore ?: 0} Pkt.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                uiState.currentLocation?.let { location ->
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "📍 Der Ort war: ${location.name}, ${location.country}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        if (uiState.isMultiplayer) {
            // Im Multiplayer wählt nicht der einzelne Spieler den Rundenwechsel-
            // Zeitpunkt, sondern die Runde wird serverseitig gesteuert,
            // sobald alle Mitspieler ihren Tipp abgegeben haben. Deshalb wird
            // hier kein interaktiver Button angezeigt, sondern lediglich ein
            // Warte-Hinweis, bis onNextRound automatisch nach Tippabgabe beider Spieler
            // ausgelöst wird.
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "⏳ Warte auf Mitspieler...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            Button(
                onClick = onNextRound,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = if (uiState.currentRound == uiState.totalRounds) {
                        "Gesamtergebnis anzeigen"
                    } else {
                        "Nächste Runde"
                    }, style = MaterialTheme.typography.titleMedium
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
    totalScore: Int, onExitGame: () -> Unit, modifier: Modifier = Modifier
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
 * Bettet die native Google-Street-View-Panorama-View über [AndroidView] in
 * Compose ein. Da [StreetViewPanoramaView] eine klassische View mit eigenem
 * Lifecycle ist, müssen dessen Lifecycle-Methoden (onCreate/onStart/...)
 * manuell an den Compose-Lifecycle gekoppelt werden – Compose selbst
 * propagiert Lifecycle-Events nicht automatisch an eingebettete Views.
 *
 * @param location Zu zeigender Standort (Latitude/Longitude).
 * @param isUserNavigationEnabled Ob der Spielende sich frei durch das
 * Panorama bewegen darf (im Hardcore-/Pro-Modus deaktivierbar).
 */
@Composable
private fun GameStreetView(
    location: GeoLocation, isUserNavigationEnabled: Boolean, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // remember ohne Key: Die View soll über die gesamte Lebensdauer dieses
    // Composables hinweg identisch bleiben, damit die native Panorama-Instanz
    // nicht bei jeder Recomposition neu erzeugt wird.
    val streetViewPanoramaView = remember { StreetViewPanoramaView(context) }

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
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(factory = { streetViewPanoramaView }, modifier = modifier.fillMaxSize()) { view ->
        // update-Block statt factory: wird bei jeder Recomposition erneut
        // ausgeführt, sodass z. B. eine Änderung von isUserNavigationEnabled
        // oder ein neuer location-Wert (Rundenwechsel) auf die bereits
        // bestehende Panorama-Instanz angewendet wird, ohne die View selbst
        // neu zu erzeugen.
        view.getStreetViewPanoramaAsync { panorama ->
            panorama.isPanningGesturesEnabled = true
            panorama.isZoomGesturesEnabled = true
            panorama.isUserNavigationEnabled = isUserNavigationEnabled
            panorama.isStreetNamesEnabled = false
            panorama.setPosition(
                LatLng(location.latitude, location.longitude), STREET_VIEW_SEARCH_RADIUS_METERS
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
        }) {
        selectedCoordinate?.let {
            Marker(
                state = rememberUpdatedMarkerState(LatLng(it.latitude, it.longitude)),
                title = "Dein Tipp"
            )
        }
    }
}

/**
 * Zeigt nach Abschluss einer Runde eine Karte mit dem tatsächlichen
 * Standort, dem abgegebenen Tipp und einer verbindenden Linie zwischen
 * beiden Punkten.
 *
 * Die Kamera zoomt und schwenkt automatisch so, dass beide Marker
 * vollständig sichtbar sind, sobald die Karte fertig geladen ist –
 * dadurch muss der Nutzer nicht manuell zoomen, um Tipp und
 * tatsächlichen Ort miteinander zu vergleichen.
 *
 * @param actualLocation Der tatsächliche, im Spiel vorgegebene Standort.
 * @param guessedLocation Die vom Nutzer abgegebene Schätzung.
 */
@Composable
private fun RoundResultMap(
    actualLocation: GeoLocation, guessedLocation: GeoCoordinate, modifier: Modifier = Modifier
) {
    val actualPosition = LatLng(
        actualLocation.latitude, actualLocation.longitude
    )

    val guessedPosition = LatLng(
        guessedLocation.latitude, guessedLocation.longitude
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            EUROPE_CENTER, INITIAL_ZOOM
        )
    }

    // Wird erst true, wenn die GoogleMap-Instanz vollständig initialisiert ist
    // (onMapLoaded-Callback). Erst danach darf die Kamera-Animation gestartet
    // werden, da CameraUpdateFactory.newLatLngBounds vor dem vollständigen
    // Laden der Karte fehlschlagen bzw. keine Wirkung zeigen kann.
    var isMapLoaded by remember {
        mutableStateOf(false)
    }

    // Umschließendes Rechteck (Bounds), das beide Punkte (tatsächlicher Ort
    // und Tipp) enthält – wird neu berechnet, sobald sich einer der beiden
    // Punkte ändert (z. B. bei einer neuen Runde).
    val bounds = remember(
        actualPosition, guessedPosition
    ) {
        LatLngBounds.Builder().include(actualPosition).include(guessedPosition).build()
    }

    // Abstand in Pixeln zwischen den Bounds und dem Kartenrand, damit die
    // Marker nicht direkt am Bildschirmrand kleben; dp->px-Umrechnung ist
    // nötig, da CameraUpdateFactory Pixel statt dp erwartet.
    val boundsPaddingPixels = with(LocalDensity.current) {
        48.dp.roundToPx()
    }

    // Startet die Kamera-Animation erst, wenn die Karte geladen ist, und
    // erneut, falls sich die Bounds (also einer der beiden Standorte) ändern.
    LaunchedEffect(isMapLoaded, bounds) {
        if (isMapLoaded) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(
                    bounds, boundsPaddingPixels
                ), durationMs = 800
            )
        }
    }

    GoogleMap(
        onMapLoaded = {
            isMapLoaded = true
        }, modifier = modifier, cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = rememberUpdatedMarkerState(actualPosition), title = "Tatsächlicher Standort"
        )

        Marker(
            state = rememberUpdatedMarkerState(guessedPosition), title = "Dein Tipp"
        )

        // Geodätische Linie (folgt der Erdkrümmung statt einer geraden
        // Bildschirmlinie) verbindet Tipp und tatsächlichen Ort visuell,
        // um die Entfernung anschaulich darzustellen.
        Polyline(
            points = listOf(
                actualPosition, guessedPosition
            ), color = Color.Red, width = 8f, geodesic = true
        )
    }
}

/**
 * Zeigt die kompakte Status-Leiste während einer laufenden Runde an:
 * aktuelle Punktzahl, Rundenzähler, verbleibende Zeit sowie – im
 * Custom-Modus mit begrenzten Leben.
 *
 * @param lives Anzahl der verbleibenden Leben im Custom-Modus. Der
 * Default [Int.MAX_VALUE] signalisiert "unbegrenzte Leben" für alle
 * Modi, die kein Leben-System verwenden; der Schwellenwert 100 in
 * [showLives] dient lediglich dazu, diesen Default sicher von einer kleinen
 * Lebensanzahl zu unterscheiden.
 */
@Composable
private fun GameStatusHeader(
    score: Int,
    currentRound: Int,
    totalRounds: Int,
    remainingSeconds: Int,
    gameMode: GameMode,
    lives: Int = Int.MAX_VALUE
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
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Score: $score")

                // Nur Einzelspieler-Custom-Modus: Battle Royale existiert
                // ausschließlich im Multiplayer und wird daher hier nie geprüft.
                val showLives = gameMode == GameMode.CUSTOM && lives < 100
                if (showLives) {
                    Text("❤️ $lives")
                }

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
                text = gameMode.displayName,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Formatiert Koordinaten für die Anzeige.
 */
private fun formatCoordinates(coordinate: GeoCoordinate): String {
    return String.format(
        Locale.US, "Lat: %.4f, Lng: %.4f", coordinate.latitude, coordinate.longitude
    )
}
