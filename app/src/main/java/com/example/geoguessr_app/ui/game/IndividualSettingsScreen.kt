package com.example.geoguessr_app.ui.game

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.domain.model.custom.CustomDifficulty
import com.example.geoguessr_app.domain.model.custom.CustomGameSettings
import com.example.geoguessr_app.domain.model.custom.Region

/**
 * Konfigurationsbildschirm für den Individuell-Modus ([GameMode.CUSTOM]).
 *
 * Lässt Spielende Zeitlimit, Region und Schwierigkeitsgrad frei wählen,
 * bevor [GameViewModel.startCustomGame] mit den resultierenden
 * [CustomGameSettings] aufgerufen wird. Die Auswahl wird ausschließlich
 * lokal per [remember] gehalten (kein ViewModel-State), da diese Werte nur
 * bis zum Klick auf "Partie starten" relevant sind und danach vollständig
 * in [CustomGameSettings] überführt werden.
 *
 * @param onBackClick Navigiert zurück, ohne eine Partie zu starten.
 * @param onStartGame Übergibt die gewählten Einstellungen und startet die Partie.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualSettingsScreen(
    onBackClick: () -> Unit,
    onStartGame: (CustomGameSettings) -> Unit
) {
    var selectedTime by remember { mutableIntStateOf(60) }
    var selectedRegion by remember { mutableStateOf(Region.WORLD) }
    var selectedDifficulty by remember { mutableStateOf(CustomDifficulty.MEDIUM) }

    val scrollState = rememberScrollState()

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
                    text = "Individuell",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            SettingsSection(title = "Zeitlimit pro Runde") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 120).forEach { time ->
                        FilterChip(
                            selected = selectedTime == time,
                            onClick = { selectedTime = time },
                            label = { Text("$time s") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            SettingsSection(title = "Region auswählen") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // In Zweiergruppen dargestellt, damit die Chips bei
                    // ungerader Anzahl an Regionen nicht die volle Breite
                    // einnehmen und optisch unruhig wirken.
                    Region.entries.chunked(2).forEach { rowRegions ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowRegions.forEach { region ->
                                FilterChip(
                                    selected = selectedRegion == region,
                                    onClick = { selectedRegion = region },
                                    label = { Text(region.displayName) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Bei einer ungeraden letzten Zeile (nur 1 statt
                            // 2 Chips) füllt ein Spacer die zweite Spalte,
                            // damit der einzelne Chip nicht auf volle Breite
                            // gestreckt wird.
                            if (rowRegions.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            SettingsSection(title = "Schwierigkeitsgrad") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomDifficulty.entries.forEach { diff ->
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = diff },
                            label = { Text(diff.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    onStartGame(
                        CustomGameSettings(
                            timeLimitSeconds = selectedTime,
                            region = selectedRegion,
                            difficulty = selectedDifficulty
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Partie starten", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/**
 * Einheitlich formatierter Abschnitt mit Titel, für die Gruppierung
 * verwandter Einstellungen (Zeitlimit, Region, Schwierigkeit).
 */
@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}