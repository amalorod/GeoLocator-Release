package com.example.geoguessr_app.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.geoguessr_app.data.dailyquest.DailyQuestRepository
import com.example.geoguessr_app.data.profile.ProfileRepository
import com.example.geoguessr_app.data.statistics.StatisticsRepository
import com.example.geoguessr_app.domain.model.custom.CustomGameSettings
import com.example.geoguessr_app.ui.game.GameMode
import com.example.geoguessr_app.ui.game.GameRoute
import com.example.geoguessr_app.ui.game.GameViewModel
import com.example.geoguessr_app.ui.game.IndividualSettingsScreen
import com.example.geoguessr_app.ui.game.MultiplayerGameRoute
import com.example.geoguessr_app.ui.home.HomeScreen
import com.example.geoguessr_app.ui.dailyquest.DailyQuestScreen
import com.example.geoguessr_app.ui.maptest.MapTestScreen
import com.example.geoguessr_app.ui.multiplayer.JoinLobbyScreen
import com.example.geoguessr_app.ui.multiplayer.LobbyViewModel
import com.example.geoguessr_app.ui.multiplayer.MultiplayerHomeScreen
import com.example.geoguessr_app.ui.multiplayer.MultiplayerLobbyScreen
import com.example.geoguessr_app.ui.profile.ProfileScreen
import com.example.geoguessr_app.ui.statistics.LifetimeStatisticsScreen
import com.example.geoguessr_app.ui.streetviewtest.StreetViewTestScreen
import com.example.geoguessr_app.ui.theme.AppThemeMode
import com.example.geoguessr_app.ui.tutorial.TutorialScreen

/**
 * Zentraler Navigationsgraph der Anwendung.
 *
 * Implementiert die Single-Activity-Architektur: Sämtliche Screens werden
 * hier als [composable]-Ziele registriert und über den [NavController]
 * gewechselt. Diese Funktion bildet damit die Wurzel des UI-Layers.
 *
 * Das GameViewModel wird bewusst hier – oberhalb der einzelnen
 * composable()-Ziele – über hiltViewModel() erzeugt, statt innerhalb der
 * Game-Route. Dadurch ist es an den Lebenszyklus des NavHost und nicht an
 * den der Game-Route gebunden. Der Nutzer kann so ins Hauptmenü wechseln
 * (Home), während die Partie im Hintergrund weiterläuft, und über
 * onResumeGameClick zur laufenden Partie zurückkehren, ohne dass der
 * Spielzustand verloren geht.
 *
 * @param selectedGameMode aktuell ausgewählter Spielmodus (z. B. Normal,
 *   Multiplayer, Custom); wird von außen (MainActivity) hereingereicht,
 *   da er auch außerhalb des NavHosts (z. B. im Home-Menü) sichtbar ist.
 * @param onGameModeSelected Callback zur Änderung des Spielmodus.
 * @param currentThemeName Anzeigename des aktiven Farbthemas.
 * @param onExitAppClick Callback zum vollständigen Beenden der App.
 * @param currentTheme aktives Farbthema als Enum-Wert.
 * @param onThemeSelected Callback zur Änderung des Farbthemas.
 */
@Composable
fun GeoGuessrNavHost(
    selectedGameMode: GameMode,
    onGameModeSelected: (GameMode) -> Unit,
    modifier: Modifier = Modifier,
    currentThemeName: String,
    onExitAppClick: () -> Unit,
    currentTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
) {
    val navController = rememberNavController()

    // Über den gesamten NavHost hinweg gültiges GameViewModel (siehe
    // Kotlin-Doc oben zur Begründung des "übergeordneten" Scopes).
    val gameViewModel: GameViewModel = hiltViewModel()

    // ARCHITEKTUR-HINWEIS: ProfileRepository wird hier als statisches
    // Singleton-Objekt angesprochen statt über Hilt injiziert zu werden.
    // Das durchbricht das DI-Konzept der restlichen App und sollte im
    // Rahmen der Data-Layer-Überarbeitung (di/-Modul) durch ein via
    // Konstruktor injiziertes Repository ersetzt werden.
    val userProfile by ProfileRepository.profile.collectAsStateWithLifecycle()

    // Merkt sich, ob im Hintergrund eine laufende Partie pausiert wurde
    // (Nutzer ist zum Home-Screen navigiert, ohne das Spiel zu beenden).
    // rememberSaveable stellt sicher, dass dieser Zustand auch eine
    // Konfigurationsänderung (z. B. Bildschirmdrehung) überlebt.
    var isGameInBackground by rememberSaveable {
        mutableStateOf(false)
    }

    // ARCHITEKTUR-HINWEIS: Das initiale Laden der App-Daten (Profil,
    // Statistiken, Daily Quests) erfolgt direkt im NavHost. Sauberer
    // wäre die Auslagerung in ein eigenes App-Start-ViewModel, da der
    // NavHost eigentlich nur für das Routing verantwortlich sein sollte
    // (Single-Responsibility-Prinzip). Für den aktuellen Projektstand
    // wurde diese pragmatische Lösung gewählt, da die geladenen Daten
    // von mehreren, unabhängigen Screens benötigt werden.
    LaunchedEffect(Unit) {
        try {
            Log.d("NAVHOST", "Starte Initialisierung...")
            // Das Profil steuert nun den gesamten Ladevorgang für Statistiken und Quests
            ProfileRepository.loadProfile()

            // Leaderboard wird nur für angemeldete Nutzer geladen (oder immer, wenn gewünscht)
            if (ProfileRepository.profile.value != null) {
                StatisticsRepository.loadLeaderboard()
            }
            Log.d("NAVHOST", "Initialisierung abgeschlossen.")
        } catch (e: Exception) {
            // Fehler beim Laden werden abgefangen, damit ein einzelner
            // fehlschlagender Repository-Aufruf nicht den Start der
            // gesamten App verhindert (Robustheit statt Absturz).
            Log.d("NAVHOST", "Fehler bei Initialisierung", e)
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
        modifier = modifier,
    ) {
        // --- Home Screen: zentraler Einstiegspunkt der App ---
        composable(route = AppDestination.Home.route) {
            HomeScreen(
                currentThemeName = currentThemeName,
                // Ein vorhandenes Profil signalisiert, dass der Nutzer
                // kein Gast ist – steuert z. B. den Profilindikator.
                isProfileSetup = userProfile != null,
                onProfileClick = {
                    navController.navigate(AppDestination.Profile.route)
                },
                onStatisticsClick = {
                    navController.navigate(AppDestination.Statistics.route)
                },
                onDailyQuestClick = {
                    navController.navigate(AppDestination.DailyQuest.route)
                },
                onExitAppClick = onExitAppClick,
                // Wechselt zyklisch zum nächsten verfügbaren Farbthema.
                onThemeClick = { onThemeSelected(currentTheme.next()) },
                hasActiveGame = isGameInBackground,
                onStartGameClick = {
                    isGameInBackground = false
                    // Verzweigung je nach gewähltem Spielmodus: Multiplayer
                    // und individueller Modus benötigen vorherige
                    // Konfigurationsschritte, der Normalmodus startet direkt.
                    if (selectedGameMode == GameMode.MULTIPLAYER) {
                        navController.navigate(AppDestination.MultiplayerHome.route)
                    } else if (selectedGameMode == GameMode.CUSTOM) {
                        navController.navigate(AppDestination.IndividualSettings.route)
                    } else {
                        gameViewModel.startNewGame(selectedGameMode)
                        navController.navigate(AppDestination.Game.route)
                    }
                },
                onResumeGameClick = {
                    // Setzt eine im Hintergrund pausierte Partie fort,
                    // statt eine neue zu starten.
                    gameViewModel.resumeGame()
                    isGameInBackground = false
                    navController.popBackStack()
                },
                onTutorialClick = {
                    navController.navigate(AppDestination.Tutorial.route)
                },
                selectedGameMode = selectedGameMode,
                onGameModeSelected = onGameModeSelected,
            )
        }

        composable(route = AppDestination.DailyQuest.route) {
            DailyQuestScreen(onBackClick = { navController.popBackStack() })
        }

        composable(route = AppDestination.Profile.route) {
            val profile by ProfileRepository.profile.collectAsStateWithLifecycle()
            ProfileScreen(
                // Fallback auf ein leeres Standardprofil, falls noch kein
                // Profil geladen wurde (z. B. Gastmodus oder Ladezustand),
                // damit ProfileScreen keinen Nullable-Typ behandeln muss.
                profile = profile ?: com.example.geoguessr_app.domain.model.profile.PlayerProfile(),
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- Multiplayer-Einstiegspunkt ---
        composable(route = AppDestination.MultiplayerHome.route) {
            MultiplayerHomeScreen(
                onBackClick = { navController.popBackStack() },
                onCreateLobbyClick = {
                    navController.navigate(AppDestination.MultiplayerLobby.route)
                },
                onJoinLobbyClick = {
                    navController.navigate(AppDestination.JoinLobby.route)
                }
            )
        }

        composable(route = AppDestination.JoinLobby.route) {
            JoinLobbyScreen(
                onBackClick = { navController.popBackStack() },
                onJoinClick = { code ->
                    // Der eingegebene Lobby-Code wird als optionaler
                    // Query-Parameter an die Lobby-Route angehängt, um
                    // zwischen "Lobby erstellen" (kein Code) und
                    // "Lobby beitreten" (mit Code) zu unterscheiden.
                    navController.navigate(
                        "${AppDestination.MultiplayerLobby.route}?lobbyCode=$code"
                    )
                }
            )
        }

        // --- Multiplayer-Lobby: dient sowohl dem Erstellen als auch dem
        // Beitreten einer Lobby, gesteuert über den optionalen Parameter
        // lobbyCode ---
        composable(
            route = "${AppDestination.MultiplayerLobby.route}?lobbyCode={lobbyCode}",
            arguments = listOf(
                navArgument("lobbyCode") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val lobbyCodeArg = backStackEntry.arguments?.getString("lobbyCode")
            val lobbyViewModel: LobbyViewModel = hiltViewModel()
            val uiState by lobbyViewModel.uiState.collectAsStateWithLifecycle()

            // Einmalige Aktion beim ersten Erreichen der Route: Je nachdem,
            // ob ein Lobby-Code übergeben wurde, wird eine neue Lobby
            // erstellt (Host) oder einer bestehenden beigetreten (Gast).
            LaunchedEffect(Unit) {
                if (lobbyCodeArg == null) {
                    lobbyViewModel.createLobby()
                } else {
                    lobbyViewModel.joinLobby(lobbyCodeArg)
                }
            }

            // Beobachtet den UI-State auf den Spielstart: Sobald der Host
            // die Partie gestartet hat (started == true) und eine gültige
            // Session-ID vorliegt, wechseln alle Lobby-Teilnehmer
            // automatisch zum Multiplayer-Spielbildschirm.
            LaunchedEffect(uiState.started, uiState.sessionId) {
                if (uiState.started && uiState.sessionId.isNotEmpty()) {
                    navController.navigate(
                        "${AppDestination.MultiplayerGame.route}/${uiState.sessionId}"
                    )
                }
            }

            MultiplayerLobbyScreen(
                lobbyCode = uiState.lobbyCode,
                players = uiState.players,
                currentUserUid = uiState.currentUserUid,
                selectedMode = uiState.selectedMode,
                errorMessage = uiState.errorMessage,
                onModeSelected = { lobbyViewModel.selectMode(it) },
                onBackClick = { navController.popBackStack() },
                onLeaveClick = {
                    lobbyViewModel.leaveLobby()
                    navController.popBackStack()
                },
                onReadyClick = { lobbyViewModel.toggleReady() },
                onStartGameClick = { lobbyViewModel.startLobby() }
            )
        }

        // --- Aktive Multiplayer-Partie, identifiziert über die
        // verpflichtende sessionId (Pfadparameter, kein Query-Parameter,
        // da hier kein sinnvoller Default existiert) ---
        composable(
            route = "${AppDestination.MultiplayerGame.route}/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            MultiplayerGameRoute(
                sessionId = sessionId,
                currentThemeName = currentThemeName,
                currentTheme = currentTheme,
                onThemeSelected = onThemeSelected,
                onHomeClick = {
                    // popUpTo mit inclusive = true entfernt auch den
                    // aktuellen Home-Eintrag aus dem Stack, sodass beim
                    // erneuten Navigieren zu Home kein doppelter Eintrag
                    // entsteht und der Zurück-Button nicht in die
                    // beendete Partie zurückführt.
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Home.route) { inclusive = true }
                    }
                },
                onStatisticsClick = {
                    navController.navigate(AppDestination.Statistics.route)
                },
                onExitGame = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = AppDestination.Tutorial.route) {
            TutorialScreen(onBackClick = navController::popBackStack)
        }

        composable(route = AppDestination.Statistics.route) {
            val statistics by StatisticsRepository.statistics.collectAsStateWithLifecycle()
            val profile by ProfileRepository.profile.collectAsStateWithLifecycle()

            LifetimeStatisticsScreen(
                statistics = statistics,
                isGuest = profile == null,
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- Einzelspieler-Partie (Normal- und Custom-Modus) ---
        composable(route = AppDestination.Game.route) {
            GameRoute(
                currentThemeName = currentThemeName,
                currentTheme = currentTheme,
                onThemeSelected = onThemeSelected,
                viewModel = gameViewModel,
                onHomeClick = {
                    // Anders als bei Multiplayer wird die Partie hier NICHT
                    // beendet, sondern nur pausiert (isGameInBackground =
                    // true), damit sie über onResumeGameClick fortgesetzt
                    // werden kann. launchSingleTop verhindert doppelte
                    // Home-Instanzen im Stack.
                    isGameInBackground = true
                    navController.navigate(AppDestination.Home.route) {
                        launchSingleTop = true
                    }
                },
                onStatisticsClick = {
                    navController.navigate(AppDestination.Statistics.route)
                },
                onExitGame = {
                    // Im Gegensatz zu onHomeClick wird die Partie hier
                    // endgültig beendet (isGameInBackground = false).
                    isGameInBackground = false
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AppDestination.IndividualSettings.route) {
            IndividualSettingsScreen(
                onBackClick = {
                    // Try-Catch fängt hier den unwahrscheinlichen Fall ab,
                    // dass kein vorheriger Backstack-Eintrag existiert
                    // (sollte durch die feste Navigationsstruktur der App
                    // eigentlich nicht auftreten, dient aber als
                    // Absicherung gegen Abstürze).
                    try {
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Log.d("NAVHOST", "Fehler beim Zurückgehen", e)
                    }
                },
                onStartGame = { settings: CustomGameSettings ->
                    try {
                        gameViewModel.startCustomGame(settings)
                        navController.navigate(AppDestination.Game.route)
                    } catch (e: Exception) {
                        Log.d("NAVHOST", "Fehler beim Spielstart", e)
                    }
                }
            )
        }

        // --- Interne Entwickler-/Testrouten, nicht über die regulä­re
        // App-Navigation erreichbar ---
        composable(route = AppDestination.MapTest.route) {
            MapTestScreen(onBackClick = navController::popBackStack)
        }

        composable(route = AppDestination.StreetViewTest.route) {
            StreetViewTestScreen(onBackClick = navController::popBackStack)
        }
    }
}
