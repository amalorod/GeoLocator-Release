package com.example.geoguessr_app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.ui.game.GameMode

/**
 * Dialog zur Auswahl des Spielmodus, der über den "MODUS: ..."-Button im
 * [HomeScreen] geöffnet wird. Zeigt alle regulären Spielmodi als
 * Radio-Button-Liste; [GameMode.BATTLE_ROYALE] wird hier bewusst
 * ausgeblendet, da dieser Modus ausschließlich über den separaten
 * Multiplayer-Einstiegspunkt und nicht über die normale Modusauswahl
 * gestartet werden kann.
 *
 * Noch nicht freigeschaltete Modi ([GameMode.isAvailable] == false) werden
 * weiterhin angezeigt, aber deaktiviert dargestellt ("bald verfügbar"),
 * damit Spielende einen Ausblick auf kommende Features erhalten, statt sie
 * komplett zu verbergen.
 *
 * @param selectedMode Aktuell ausgewählter Spielmodus (vorbelegte Radio-Auswahl).
 * @param onModeSelected Callback bei Auswahl eines verfügbaren Modus; schließt den Dialog implizit über [onDismiss].
 * @param onDismiss Schließt den Dialog ohne Änderung der Auswahl.
 */
@Composable
fun GameModeDialog(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Spielmodus auswählen") },
        text = {
            Column {
                GameMode.entries
                    .filter { it != GameMode.BATTLE_ROYALE }
                    .forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                // Nicht verfügbare Modi sind nicht klickbar,
                                // werden aber weiterhin sichtbar in der Liste
                                // geführt (siehe Klassen-KDoc).
                                .clickable(enabled = mode.isAvailable) {
                                    onModeSelected(mode)
                                    onDismiss()
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mode == selectedMode,
                                // onClick = null, da der Klick bereits von der
                                // umgebenden Row via .clickable() behandelt wird;
                                // ein zusätzlicher eigener Klick-Handler am
                                // RadioButton würde denselben Vorgang doppelt
                                // auslösen bzw. inkonsistent mit dem restlichen
                                // Zeileninhalt reagieren.
                                onClick = null,
                                enabled = mode.isAvailable
                            )

                            Text(
                                text = if (mode.isAvailable) {
                                    mode.displayName
                                } else {
                                    "${mode.displayName} · bald verfügbar"
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}