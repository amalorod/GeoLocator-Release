package alic.malorodow.geoguessr_app.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import alic.malorodow.geoguessr_app.MainActivity
import alic.malorodow.geoguessr_app.domain.model.custom.CustomGameSettings
import alic.malorodow.geoguessr_app.domain.model.profile.PlayerProfile
import alic.malorodow.geoguessr_app.ui.components.ThemeSelectorMenu
import alic.malorodow.geoguessr_app.ui.dailyquest.DailyQuestScreen
import alic.malorodow.geoguessr_app.ui.game.ChallengeSelectionScreen
import alic.malorodow.geoguessr_app.ui.game.GameMode
import alic.malorodow.geoguessr_app.ui.game.GameRoute
import alic.malorodow.geoguessr_app.ui.game.GameViewModel
import alic.malorodow.geoguessr_app.ui.game.IndividualSettingsScreen
import alic.malorodow.geoguessr_app.ui.game.MultiplayerGameRoute
import alic.malorodow.geoguessr_app.ui.home.HomeScreen
import alic.malorodow.geoguessr_app.ui.maptest.MapTestScreen
import alic.malorodow.geoguessr_app.ui.multiplayer.JoinLobbyScreen
import alic.malorodow.geoguessr_app.ui.multiplayer.LobbyViewModel
import alic.malorodow.geoguessr_app.ui.multiplayer.MultiplayerHomeScreen
import alic.malorodow.geoguessr_app.ui.multiplayer.MultiplayerLobbyScreen
import alic.malorodow.geoguessr_app.ui.profile.ProfileScreen
import alic.malorodow.geoguessr_app.ui.profile.ProfileViewModel
import alic.malorodow.geoguessr_app.ui.statistics.LifetimeStatisticsScreen
import alic.malorodow.geoguessr_app.ui.statistics.StatisticsViewModel
import alic.malorodow.geoguessr_app.ui.streetviewtest.StreetViewTestScreen
import alic.malorodow.geoguessr_app.ui.theme.AppThemeMode
import alic.malorodow.geoguessr_app.ui.theme.ThemeViewModel
import alic.malorodow.geoguessr_app.ui.tutorial.TutorialScreen
import alic.malorodow.geoguessr_app.ui.welcome.OnboardingViewModel
import alic.malorodow.geoguessr_app.ui.welcome.WelcomeScreen

/**
 * Zentraler Navigationsgraf der Anwendung.
 *
 * Verwaltet neben der reinen Bildschirmnavigation zwei über den gesamten
 * Graphen hinweg gültige, app-weite Zustände: den Theme-/Dynamic-Color-
 * Zustand (siehe [currentTheme]/[currentDynamicColorEnabled], gesetzt in
 * [MainActivity] über das app-weite [ThemeViewModel]) sowie [isGameInBackground()]
 * zur Unterscheidung zwischen einer pausierten und einer endgültig beendeten 
 * Einzelspieler-Partie.
 *
 * @param currentTheme Aktuell aktives, festes Farbschema.
 * @param currentDynamicColorEnabled Ob stattdessen die Material-You-Systemfarbe verwendet wird.
 * @param onThemeSelected Callback bei Auswahl eines festen Farbschemas; wird unverändert
 * bis zu [ThemeSelectorMenu] in HomeScreen und GameScreen durchgereicht.
 * @param onDynamicColorToggled Callback beim Umschalten der Material-You-Systemfarbe.
 */
@Composable
fun GeoGuessrNavHost(
    selectedGameMode: GameMode,
    onGameModeSelected: (GameMode) -> Unit,
    modifier: Modifier = Modifier,
    currentTheme: AppThemeMode,
    currentDynamicColorEnabled: Boolean,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDynamicColorToggled: (Boolean) -> Unit,
    onExitAppClick: () -> Unit,
) {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val hasSeenTutorial by onboardingViewModel.hasSeenTutorial.collectAsStateWithLifecycle(
        initialValue = null
    )

    // Über den gesamten NavHost hinweg gültige ViewModels
    val gameViewModel: GameViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val statisticsViewModel: StatisticsViewModel = hiltViewModel()

    val userProfile by profileViewModel.profile.collectAsStateWithLifecycle()

    // Merkt sich, ob im Hintergrund eine laufende Partie pausiert wurde
    var isGameInBackground by rememberSaveable {
        mutableStateOf(false)
    }

    // Lädt einmalig beim ersten Aufbau des NavHost das Gast-/Nutzerprofil
    // aus DataStore oder Firebase, damit userProfile bereits beim
    // Rendern der ersten Screens (Home, Statistics) zur Verfügung steht.
    // Fehler werden geloggt statt geworfen, damit ein einzelner
    // fehlgeschlagener Ladevorgang nicht den gesamten Navigationsaufbau
    // (und damit die App) abstürzen lässt.
    LaunchedEffect(Unit) {
        try {
            Log.d("NAVHOST", "Starte Initialisierung...")
            profileViewModel.loadInitialData()
            Log.d("NAVHOST", "Initialisierung abgeschlossen.")
        } catch (e: Exception) {
            Log.d("NAVHOST", "Fehler bei Initialisierung", e)
        }
    }

    // Solange hasSeenTutorial noch nicht geladen ist (null), wird kein
    // NavHost gezeichnet, um ein kurzes Aufblitzen von Home vor dem
    // eigentlichen Welcome-Screen zu vermeiden.
    if (hasSeenTutorial == null) return

    // Einmalig eingefrorene Start-Destination: Verhindert, dass eine
    // Recomposition bei StateFlow-Änderungen (z. B. nach Klick auf "Home"
    // im Onboarding) eine ungültige NavHost-Graphenrekonfiguration auslöst.
    val initialStartDestination = remember {
        if (hasSeenTutorial == true) AppDestination.Home.route else AppDestination.Welcome.route
    }

    NavHost(
        navController = navController,
        startDestination = initialStartDestination,
        modifier = modifier,
    ) {
        // --- Welcome Screen: zentraler Einstiegspunkt der App für neue User ---
        composable(route = AppDestination.Welcome.route) {
            WelcomeScreen(
                onOnboardingFinished = {
                    onboardingViewModel.markTutorialAsSeen()
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        // --- Home Screen: zentraler Einstiegspunkt der App ---
        composable(route = AppDestination.Home.route) {
            HomeScreen(
                currentTheme = currentTheme,
                currentDynamicColorEnabled = currentDynamicColorEnabled,
                onThemeSelected = onThemeSelected,
                onDynamicColorToggled = onDynamicColorToggled,
                // Ein vorhandenes Profil signalisiert, dass der Nutzer
                // kein Gast ist und steuert den farbigen Profilindikator.
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
                    } else if (selectedGameMode == GameMode.CHALLENGE) {
                        navController.navigate(AppDestination.ChallengeSelection.route)
                    } else {
                        gameViewModel.startNewGame(selectedGameMode)
                        navController.navigate(AppDestination.Game.route)
                    }
                },
                onResumeGameClick = {
                    // Setzt eine im Hintergrund pausierte Partie fort, statt eine neue
                    // zu starten. popBackStack() (statt navigate) funktioniert hier, weil
                    // die Game-Route beim Pausieren nicht aus dem Backstack entfernt
                    // wurde – der Nutzer kehrt also einfach zur bereits bestehenden,
                    // weiterhin aktiven Game-Instanz zurück, statt eine neue zu erzeugen.
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
            val profile by profileViewModel.profile.collectAsStateWithLifecycle()
            ProfileScreen(
                profile = profile ?: PlayerProfile(),
                onBackClick = { navController.popBackStack() })
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
                })
        }

        composable(route = AppDestination.JoinLobby.route) {
            JoinLobbyScreen(onBackClick = { navController.popBackStack() }, onJoinClick = { code ->
                // Der eingegebene Lobby-Code wird als optionaler
                // Query-Parameter an die Lobby-Route angehängt, um
                // zwischen "Lobby erstellen" (kein Code) und
                // "Lobby beitreten" (mit Code) zu unterscheiden.
                navController.navigate(
                    "${AppDestination.MultiplayerLobby.route}?lobbyCode=$code"
                )
            })
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
                })
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
                onStartGameClick = { lobbyViewModel.startLobby() })
        }

        // --- Aktive Multiplayer-Partie, identifiziert über die
        // verpflichtende sessionId (Pfadparameter, kein Query-Parameter,
        // da hier kein sinnvoller Default existiert) ---
        composable(
            route = "${AppDestination.MultiplayerGame.route}/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            // Fallback auf einen leeren String nur als Absicherung gegen einen
            // fehlenden Pflichtparameter. Normalerweise garantiert die
            // Navigation stets einen gesetzten sessionId-Wert, da die Route ohne
            // diesen Parameter gar nicht erreichbar ist.

            MultiplayerGameRoute(
                sessionId = sessionId,
                currentTheme = currentTheme,
                currentDynamicColorEnabled = currentDynamicColorEnabled,
                onThemeSelected = onThemeSelected,
                onDynamicColorToggled = onDynamicColorToggled,
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
                    // Gleiches Verhalten wie onHomeClick: Entfernt auch den aktuellen
                    // Home-Eintrag aus dem Stack (inclusive = true), damit nach dem
                    // Verlassen der beendeten Partie kein doppelter Home-Eintrag im
                    // Backstack verbleibt und der Zurück-Button nicht erneut in die
                    // bereits beendete Partie zurückführt.
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Home.route) { inclusive = true }
                    }
                })
        }


        composable(route = AppDestination.Tutorial.route) {
            TutorialScreen(
                onFinish = navController::popBackStack,
                onBackClick = navController::popBackStack
            )
        }

        composable(route = AppDestination.Statistics.route) {
            val statistics by statisticsViewModel.statistics.collectAsStateWithLifecycle()
            val profile by profileViewModel.profile.collectAsStateWithLifecycle()

            LifetimeStatisticsScreen(
                statistics = statistics,
                isGuest = profile == null,
                onBackClick = { navController.popBackStack() })
        }

        // --- Einzelspieler-Partie (Normal- und Custom-Modus) ---
        composable(route = AppDestination.Game.route) {
            GameRoute(
                currentTheme = currentTheme,
                currentDynamicColorEnabled = currentDynamicColorEnabled,
                onThemeSelected = onThemeSelected,
                onDynamicColorToggled = onDynamicColorToggled,
                viewModel = gameViewModel,
                onHomeClick = {
                    // Anders als bei Multiplayer wird die Partie hier nicht
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
                    // inclusive = false (anders als bei MultiplayerGame.onExitGame, wo
                    // inclusive = true verwendet wird): Der bestehende Home-Eintrag im
                    // Stack bleibt hier erhalten und wird nicht entfernt, da Home in
                    // diesem Fall bereits der unterste Eintrag ist und kein doppelter
                    // Home-Eintrag entstehen kann. launchSingleTop reicht hier aus, um
                    // Duplikate zu vermeiden.
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                })
        }

        composable(route = AppDestination.IndividualSettings.route) {
            IndividualSettingsScreen(onBackClick = {
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
            }, onStartGame = { settings: CustomGameSettings ->
                try {
                    gameViewModel.startCustomGame(settings)
                    navController.navigate(AppDestination.Game.route)
                } catch (e: Exception) {
                    Log.d("NAVHOST", "Fehler beim Spielstart", e)
                }
            })
        }

        composable(route = AppDestination.ChallengeSelection.route) {
            ChallengeSelectionScreen(onBackClick = {
                try {
                    navController.popBackStack()
                } catch (e: Exception) {
                    Log.d("NAVHOST", "Fehler beim Zurückgehen", e)
                }
            }, onStartGame = { settings: CustomGameSettings ->
                try {
                    gameViewModel.startCustomGame(settings)
                    navController.navigate(AppDestination.Game.route)
                } catch (e: Exception) {
                    Log.d("NAVHOST", "Fehler beim Spielstart", e)
                }
            })
        }

        // --- Interne Testrouten, nicht über die reguläre App-Navigation erreichbar ---
        composable(route = AppDestination.MapTest.route) {
            MapTestScreen(onBackClick = navController::popBackStack)
        }

        composable(route = AppDestination.StreetViewTest.route) {
            StreetViewTestScreen(onBackClick = navController::popBackStack)
        }
    }
}