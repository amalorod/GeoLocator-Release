package com.example.geoguessr_app.ui.dailyquest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.geoguessr_app.domain.model.dailyquest.DailyQuest
import com.example.geoguessr_app.ui.theme.AppThemeMode

/**
 * Übersicht aller Daily Quests mit Fortschrittsanzeige.
 *
 * Zeigt einen Ladeindikator, solange [DailyQuestViewModel.quests] noch
 * leer ist (erster Emit vom Repository steht noch aus), und andernfalls
 * die vollständige Liste als scrollbare [LazyColumn].
 */
@Composable
fun DailyQuestScreen(
    onBackClick: () -> Unit,
    viewModel: DailyQuestViewModel = hiltViewModel()
) {
    val quests by viewModel.quests.collectAsState()

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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Quests",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (quests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quests, key = { it.id }) { quest ->
                    QuestItem(quest)
                }
            }
        }
    }
}

/**
 * Einzelne Quest-Karte mit Icon, Titel, Beschreibung sowie entweder einem
 * Fortschrittsbalken (offene Quest) oder einem Häkchen (abgeschlossene Quest).
 *
 * Farben werden ausschließlich über [MaterialTheme.colorScheme] bezogen,
 * damit abgeschlossene Quests auch bei aktivem Dynamic Color oder einem der
 * fünf festen Farbschemata (siehe [AppThemeMode])
 * konsistent als "positiv/erfolgreich" erkennbar bleiben, statt mit fest
 * kodiertem Grün zu kollidieren.
 */
@Composable
private fun QuestItem(quest: DailyQuest) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (quest.completed) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = quest.icon, fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (quest.completed) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = quest.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (!quest.completed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val progressFraction = if (quest.target > 0) {
                        quest.progress.toFloat() / quest.target.toFloat()
                    } else {
                        0f
                    }.coerceIn(0f, 1f)

                    // Aktuelle Material3-API erwartet progress als Lambda
                    // statt als direkten Wert (vermeidet unnötige
                    // Rekompositionen der gesamten Funktion bei jeder
                    // Fortschrittsänderung); ersetzt die zuvor per
                    // @Suppress("DEPRECATION") stummgeschaltete alte Signatur.
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            if (quest.completed) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "✅", fontSize = 24.sp)
            }
        }
    }
}