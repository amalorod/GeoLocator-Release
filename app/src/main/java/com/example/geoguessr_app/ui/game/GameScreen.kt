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
        onSubmitGuess = viewModel::submitGuess,
        onNextRound = viewModel::startNextRound,
        onExitGame = onExitGame,
        onRetryLoading = viewModel::retryLoading,
        modifier = modifier
    )
}

/**
 * Zustandslose Darstellung des Spiels.
 *
 * Bis zur Einbindung der Weltkarte wird ein fester Testtipp verwendet.
 * Die UI enthält dabei selbst keine Distanz- oder Punkteberechnung.
 */
@Composable
private fun GameScreen(
    uiState: GameUiState,
    onSubmitGuess: (GeoCoordinate) -> Unit, // KORREKTUR: ":" statt "=" verwendet
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
            space = 20.dp,
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
                Text(
                    text = "Standorte werden geladen...",
                    style = MaterialTheme.typography.bodyLarge
                )
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
                Text(
                    text = "Spiel beendet! Endstand: ${uiState.totalScore} Punkte",
                    style = MaterialTheme.typography.headlineSmall
                )
                Button(onClick = onExitGame) {
                    Text("Zum Hauptmenü")
                }
            }
            uiState.isRoundFinished -> {
                Text(
                    text = uiState.roundScore?.let { "Punkte für diese Runde: $it" } ?: "Zeit abgelaufen!",
                    style = MaterialTheme.typography.bodyLarge
                )
                uiState.roundDistanceKilometers?.let {
                    Text(text = "Entfernung: %.2f km".format(it))
                }
                Button(onClick = onNextRound) {
                    Text("Nächste Runde")
                }
            }
            else -> {
                // Test-Tipp abgeben (wird später durch die Map-Auswahl ersetzt)
                Button(
                    onClick = { onSubmitGuess(GeoCoordinate(0.0, 0.0)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test-Tipp abgeben (0.0, 0.0)")
                }
            }
        }
    }
}
