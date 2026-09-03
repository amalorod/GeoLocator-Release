package com.example.geoguessr_app.ui.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.domain.model.multiplayer.LobbyPlayer
import com.example.geoguessr_app.domain.model.multiplayer.MultiplayerMode

/**
 * Lobby-Warteraum vor Spielbeginn: zeigt den Beitritts-Code, alle
 * Teilnehmer mit Ready-Status, die Spielmodus-Auswahl (nur für den Host
 * bedienbar) sowie den Ready-/Start-Button.
 *
 * Rein zustandslose Darstellung – der gesamte Zustand (Spielerliste,
 * Ready-Status, Fehler) wird von [LobbyViewModel] über die Lobby-Route in
 * [GeoGuessrNavHost] hereingereicht; diese Composable trifft selbst keine
 * Firebase-Aufrufe.
 *
 * @param lobbyCode Anzuzeigender, kopierbarer Beitritts-Code.
 * @param players Aktueller Teilnehmerstatus aller Lobby-Mitglieder.
 * @param currentUserUid UID des lokalen Nutzers; bestimmt [isHost]/[isReady].
 * @param selectedMode Aktuell vom Host gewählter Spielmodus.
 * @param errorMessage Fehlermeldung (z. B. Lobby nicht gefunden); ersetzt bei
 * Vorhandensein den gesamten Lobby-Inhalt durch eine Fehleransicht mit
 * Rückkehr-Button, da eine fehlerhafte Lobby ohnehin nicht weiter bedienbar ist.
 * @param onModeSelected Callback bei Moduswahl; wirkt nur, wenn der lokale
 * Nutzer Host ist (siehe [ModeButton]-Aufruf).
 * @param onBackClick Navigiert zurück (auch im Fehlerfall verwendet).
 * @param onLeaveClick Verlässt die Lobby aktiv über den "Verlassen"-Button im Header.
 * @param onReadyClick Schaltet den eigenen Ready-Status um.
 * @param onStartGameClick Startet die Partie; nur für den Host sichtbar und
 * nur aktivierbar, wenn mindestens zwei Spieler alle ready sind.
 */
@Composable
fun MultiplayerLobbyScreen(
    lobbyCode: String,
    players: List<LobbyPlayer>,
    currentUserUid: String,
    selectedMode: MultiplayerMode,
    errorMessage: String? = null,
    onModeSelected: (MultiplayerMode) -> Unit,
    onBackClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onReadyClick: () -> Unit,
    onStartGameClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val isHost = players.find { it.uid == currentUserUid }?.host ?: false
    val isReady = players.find { it.uid == currentUserUid }?.ready ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
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
                    text = "Multiplayer",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.weight(1f))
                // Getrennt von onBackClick: onLeaveClick meldet den Spieler
                // aktiv aus der Lobby ab (siehe LobbyViewModel.leaveLobby),
                // während onBackClick nur navigiert, ohne die Lobby zu verlassen.
                TextButton(
                    onClick = onLeaveClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Verlassen")
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            if (errorMessage != null) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Zurück zum Hauptmenü")
                }
            } else {
                Text(
                    text = "Lobby-Code kopieren",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Read-only-Textfeld statt reinem Text, um die vertraute
                // Optik eines "Code-Felds" zu erzeugen und die Kopieren-
                // Aktion direkt als trailingIcon anzubieten.
                OutlinedTextField(
                    value = lobbyCode,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = {
                            clipboardManager.setText(AnnotatedString(lobbyCode))
                        }) {
                            Text("Kopieren")
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Feste Obergrenze von 4 Spielern, analog zu
                // MultiplayerScoreboard.players.take(4) für Tablets.
                Text(
                    text = "Spieler (${players.size} / 4)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                players.forEach { player ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (player.host) "${player.name} 👑" else player.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (player.uid == currentUserUid) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )

                            // Bewusst feste Farben statt MaterialTheme.colorScheme:
                            // Grün/Grau dienen hier als universell verständliche
                            // Ampel-Metapher (bereit/nicht bereit), unabhängig
                            // vom aktiven Farbschema.
                            Text(
                                text = if (player.ready) "BEREIT ✅" else "Warten... ⏳",
                                color = if (player.ready) Color(0xFF2E7D32) else Color.Gray,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Spielmodus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Alle Modi werden angezeigt (auch BATTLE_ROYALE, das laut
                // GameMode.isAvailable im Einzelspieler-Kontext noch nicht
                // spielbar ist) – im Multiplayer-Kontext gilt eine eigene,
                // von GameMode unabhängige Verfügbarkeit über MultiplayerMode.
                MultiplayerMode.entries.forEach { mode ->
                    ModeButton(
                        mode = mode,
                        selected = mode == selectedMode,
                        // Nur der Host darf den Modus ändern; bei Nicht-Host
                        // ist der Klick ein No-Op, der Button bleibt aber
                        // sichtbar, damit alle Teilnehmer den aktuell
                        // gewählten Modus sehen.
                        onClick = { if (isHost) onModeSelected(mode) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onReadyClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReady) Color.DarkGray else Color(0xFF1565C0)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        if (isReady) "Nicht bereit" else "Ich bin bereit!",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (isHost) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val allReady = players.size >= 2 && players.all { it.ready }
                    Button(
                        onClick = onStartGameClick,
                        enabled = allReady,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allReady) Color(0xFFC62828) else Color.Gray
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Spiel starten", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    // Nicht-Host-Spieler sehen statt des Start-Buttons nur
                    // einen Hinweis, da ausschließlich der Host die Partie
                    // starten darf (siehe LobbyViewModel.startLobby).
                    Text(
                        text = "Warte auf den Host...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/** Einzelner, umschaltbarer Modus-Button innerhalb der Spielmodus-Auswahl. */
@Composable
private fun ModeButton(
    mode: MultiplayerMode,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 4.dp else 0.dp)
    ) {
        Text(mode.displayName, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}