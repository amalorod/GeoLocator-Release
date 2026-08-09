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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Route verbindet das Hilt-ViewModel mit dem zustandslosen Screen.
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
        onSubmitGuess = viewModel::submitGuess,
        onNextRound = viewModel::startNextRound,
        onExitGame = onExitGame,
        onRetryLoading = viewModel::retryLoading,
        modifier = modifier
    )
}

/**
 * Zustandslose UI. Sämtliche Daten und Aktionen werden übergeben.
 */
@Composable
private fun GameScreen(
    uiState: GameUiState,
    onSubmitGuess: () -> Unit,
    onNextRound: () -> Unit,
    onExitGame: () -> Unit,
    onRetryLoading: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 24.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        Text(
            text = "Runde ${uiState.currentRound} von ${uiState.totalRounds}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Verbleibende Zeit: ${uiState.remainingSeconds} Sekunden",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Gesamtpunkte: ${uiState.totalScore}",
            style = MaterialTheme.typography.titleLarge
        )

        when {
            uiState.isLoading -> {
                Text("Standorte werden geladen …")
            }

            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )

                Button(onClick = onRetryLoading) {
                    Text("Erneut versuchen")
                }
            }

            uiState.isGameFinished -> {
                Text(
                    text = "Spiel beendet!",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text("Endpunktzahl: ${uiState.totalScore}")

                Button(
                    onClick = onExitGame,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zurück zum Start")
                }
            }

            uiState.isRoundFinished -> {
                Text("Runde abgeschlossen")

                Button(
                    onClick = onNextRound,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (uiState.currentRound == uiState.totalRounds) {
                            "Ergebnis anzeigen"
                        } else {
                            "Nächste Runde"
                        }
                    )
                }
            }

            else -> {
                Text("Zufälliger Standort wurde geladen.")

                Button(
                    onClick = onSubmitGuess,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tipp simulieren")
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
