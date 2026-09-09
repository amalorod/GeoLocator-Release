package alic.malorodow.geoguessr_app.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import alic.malorodow.geoguessr_app.domain.model.custom.CustomGameSettings
import alic.malorodow.geoguessr_app.domain.model.custom.PredefinedChallenges

/**
 * Zeigt eine scrollbare Liste aller vorgefertigten Challenges
 * ([PredefinedChallenges.all]) an, aus denen der Nutzer eine Challenge
 * auswählen und direkt starten kann.
 *
 * Rein zustandslos (stateless) im Sinne von State Hoisting: Der
 * Screen besitzt selbst keinen veränderlichen Zustand, sondern
 * reicht Nutzerinteraktionen ausschließlich über die beiden
 * Callback-Parameter nach oben an den Aufrufer (NavHost oder
 * ViewModel) weiter. Dadurch bleibt der Composable leicht testbar
 * und unabhängig von der konkreten Navigations- oder Spiellogik.
 *
 * @param onBackClick Wird ausgelöst, wenn der Nutzer über den
 * "Zurück"-Button den Screen verlassen möchte (typischerweise
 * `navController.popBackStack()` im Aufrufer).
 * @param onStartGame Wird mit den [CustomGameSettings] der ausgewählten
 * Challenge aufgerufen, sobald der Nutzer auf "Challenge starten"
 * klickt. Der Aufrufer ist dafür verantwortlich, mit diesen
 * Einstellungen die eigentliche Spielrunde zu starten.
 */
@Composable
fun ChallengeSelectionScreen(
    onBackClick: () -> Unit,
    onStartGame: (CustomGameSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Kopfzeile mit Zurück-Button und Titel, konsistent zum
        // restlichen App-Design gestaltet.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBackClick) {
                    Text(" Zurück ", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Vorgefertigte Challenges",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // LazyColumn statt Column verwendet, um bei einer wachsenden
        // Anzahl an Challenges nur die sichtbaren Karten zu rendern
        // (Performance-Vorteil bei langen Listen).
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(PredefinedChallenges.all) { challenge ->
                // Jede Challenge wird als eigenständige Karte dargestellt,
                // die Icon, Titel, Beschreibung und einen Start-Button enthält.
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = challenge.icon, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = challenge.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = challenge.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Beim Klick werden die vordefinierten Settings der
                        // Challenge direkt an den Aufrufer übergeben, ohne
                        // dass der Screen selbst Spiellogik kennen muss.
                        Button(
                            onClick = { onStartGame(challenge.settings) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Challenge starten")
                        }
                    }
                }
            }
        }
    }
}