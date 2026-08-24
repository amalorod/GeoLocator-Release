package com.example.geoguessr_app.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.geoguessr_app.ui.game.GameMode
import com.example.geoguessr_app.ui.game.GameRoute
import com.example.geoguessr_app.ui.game.GameViewModel
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
 * Zentrale Navigation der App.
 *
 * Das GameViewModel wird oberhalb der einzelnen Ziele erzeugt.
 * Dadurch bleibt die aktive Partie beim Wechsel zum Hauptmenü erhalten.
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
    val gameViewModel: GameViewModel = hiltViewModel()
    val userProfile by ProfileRepository.profile.collectAsStateWithLifecycle()

    var isGameInBackground by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        StatisticsRepository.loadStatistics()
        ProfileRepository.loadProfile()
        DailyQuestRepository.loadQuests()
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
        modifier = modifier,
    ) {
        composable(route = AppDestination.Home.route) {
            HomeScreen(
                currentThemeName = currentThemeName,
                isProfileSetup = userProfile != null,
                onProfileClick = {
                    navController.navigate(
                        AppDestination.Profile.route
                    )
                },
                onStatisticsClick = {
                    navController.navigate(
                        AppDestination.Statistics.route
                    )
                },
                onDailyQuestClick = {
                    navController.navigate(
                        AppDestination.DailyQuest.route
                    )
                },
                onExitAppClick = onExitAppClick,
                onThemeClick = { onThemeSelected(currentTheme.next()) },
                hasActiveGame = isGameInBackground,
                onStartGameClick = {
                    isGameInBackground = false
                    if (selectedGameMode == GameMode.MULTIPLAYER) {
                        navController.navigate(
                            AppDestination.MultiplayerHome.route
                        )
                    } else {
                        gameViewModel.startNewGame(selectedGameMode)
                        navController.navigate(AppDestination.Game.route)
                    }
                },
                onResumeGameClick = {
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

        composable(
            route = AppDestination.DailyQuest.route
        ) {
            DailyQuestScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AppDestination.Profile.route
        ) {
            val profile by ProfileRepository.profile.collectAsStateWithLifecycle()
            ProfileScreen(
                profile = profile ?: com.example.geoguessr_app.domain.model.profile.PlayerProfile(),
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AppDestination.MultiplayerHome.route
        ) {
            MultiplayerHomeScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCreateLobbyClick = {
                    navController.navigate(
                        AppDestination.MultiplayerLobby.route
                    )
                },
                onJoinLobbyClick = {
                    navController.navigate(
                        AppDestination.JoinLobby.route
                    )
                }
            )
        }

        composable(
            route = AppDestination.JoinLobby.route
        ) {
            JoinLobbyScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onJoinClick = { code ->
                    navController.navigate(
                        "${AppDestination.MultiplayerLobby.route}?lobbyCode=$code"
                    )
                }
            )
        }

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

            Log.e(
                "MULTIPLAYER",
                "ROUTE ERREICHT | lobbyCodeArg=$lobbyCodeArg"
            )

            LaunchedEffect(Unit) {

                Log.e(
                    "MULTIPLAYER",
                    "LaunchedEffect (Unit) ausgeführt"
                )

                if (lobbyCodeArg == null) {
                    Log.e(
                        "MULTIPLAYER",
                        "NavHost -> createLobby"
                    )
                    lobbyViewModel.createLobby()
                } else {
                    Log.e(
                        "MULTIPLAYER",
                        "NavHost -> joinLobby($lobbyCodeArg)"
                    )
                    lobbyViewModel.joinLobby(lobbyCodeArg)
                }
            }

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
                onBackClick = {
                    navController.popBackStack()
                },
                onLeaveClick = {
                    lobbyViewModel.leaveLobby()
                    navController.popBackStack()
                },
                onReadyClick = {
                    lobbyViewModel.toggleReady()
                },
                onStartGameClick = {
                    lobbyViewModel.startLobby()
                }
            )
        }

        composable(
            route = "${AppDestination.MultiplayerGame.route}/{sessionId}",
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            MultiplayerGameRoute(
                sessionId = sessionId,
                currentThemeName = currentThemeName,
                currentTheme = currentTheme,
                onThemeSelected = onThemeSelected,
                onHomeClick = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Home.route) { inclusive = true }
                    }
                },
                onExitGame = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = AppDestination.Tutorial.route) {
            TutorialScreen(
                onBackClick = navController::popBackStack
            )
        }

        composable(
            route = AppDestination.Statistics.route
        ) {
            val statistics = StatisticsRepository.statistics.collectAsState()
            LifetimeStatisticsScreen(
                statistics = statistics.value,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppDestination.Game.route) {
            GameRoute(
                currentThemeName = currentThemeName,
                currentTheme = currentTheme,
                onThemeSelected = onThemeSelected,
                viewModel = gameViewModel,
                onHomeClick = {
                    isGameInBackground = true
                    navController.navigate(AppDestination.Home.route) {
                        launchSingleTop = true
                    }
                },
                onExitGame = {
                    isGameInBackground = false
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AppDestination.MapTest.route) {
            MapTestScreen(
                onBackClick = navController::popBackStack
            )
        }

        composable(route = AppDestination.StreetViewTest.route) {
            StreetViewTestScreen(
                onBackClick = navController::popBackStack
            )
        }
    }
}
