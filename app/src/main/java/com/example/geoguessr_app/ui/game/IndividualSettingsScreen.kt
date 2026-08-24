package com.example.geoguessr_app.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.domain.model.custom.CustomDifficulty
import com.example.geoguessr_app.domain.model.custom.CustomGameSettings
import com.example.geoguessr_app.domain.model.custom.Region

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
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Individuelles Spiel",
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
            // Zeitlimit
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

            // Region
            SettingsSection(title = "Region auswählen") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            if (rowRegions.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            // Schwierigkeit
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
