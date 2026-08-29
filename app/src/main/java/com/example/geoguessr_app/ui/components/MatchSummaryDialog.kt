package com.example.geoguessr_app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.domain.statistics.GameStatistics
import com.example.geoguessr_app.domain.statistics.RoundStatistics

/**
 * Zeigt nach Abschluss einer Partie eine Zusammenfassung der wichtigsten
 * Ergebnisse an (siehe Doku, Kapitel 4.2 „Game Screen“).
 *
 * ARCHITEKTUR-HINWEIS: Diese Datei enthält eine @Composable-Funktion
 * und importiert Jetpack-Compose-APIs, liegt aber im
 * domain/statistics-Package. Das widerspricht dem Clean-Architecture-
 * Prinzip, dass der Domain-Layer frei von UI-Framework-Abhängigkeiten
 * bleiben soll (siehe Doku, Kapitel 2.1 „Architektur“). Fachlich
 * korrekt wäre die Verschiebung dieser Funktion in den UI-Layer, z. B.
 * nach ui/components/ oder ui/game/, da sie inhaltlich eine reine
 * Darstellungskomponente ist und keine Geschäftslogik enthält.
 *
 * @param statistics Ergebnis der soeben beendeten Partie.
 * @param onDetailsClick Callback, um eine detailliertere Rundenübersicht
 *   zu öffnen.
 * @param onNewGameClick Callback, um direkt eine neue Partie zu starten.
 * @param onHomeClick Callback, um zum Home Screen zurückzukehren.
 */
@Composable
fun MatchSummaryDialog(
    statistics: GameStatistics,
    onDetailsClick: () -> Unit,
    onNewGameClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    // Ermittelt die Runde mit der höchsten Punktzahl zur Anzeige im
    // Dialog; null, falls keine Runden vorhanden sind (Randfall).
    val bestRound = statistics.rounds.maxByOrNull { it.score }

    // Division durch Null wird durch die explizite Prüfung auf eine
    // leere Rundenliste verhindert, statt sich auf eine implizite
    // Ausnahmebehandlung zu verlassen.
    val averageScore = if (statistics.rounds.isEmpty()) {
        0
    } else {
        statistics.totalScore / statistics.rounds.size
    }

    // onDismissRequest bewusst leer gelassen: Der Dialog soll nicht
    // durch Tippen außerhalb oder die Zurück-Taste geschlossen werden
    // können, sondern ausschließlich über eine der drei angebotenen
    // Aktionen (Details, Neues Spiel, Home).
    AlertDialog(onDismissRequest = {}, title = {
        Text("🏆 Partie beendet")
    }, text = {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Gesamtpunkte: ${statistics.totalScore}")
            Text(text = "Beste Runde: ${bestRound?.score ?: 0}")
            Text(text = "Ø Punkte: $averageScore")
        }
    }, confirmButton = {
        // Alle drei Aktionen werden im confirmButton-Slot statt im
        // regulären dismissButton-Slot untergebracht, da AlertDialog
        // technisch nur einen einzigen dismissButton-Slot vorsieht,
        // hier aber drei gleichwertige Optionen benötigt werden.
        Column(
            horizontalAlignment = Alignment.End
        ) {
            TextButton(onClick = onDetailsClick) {
                Text("Details")
            }
            TextButton(onClick = onNewGameClick) {
                Text("Neues Spiel")
            }
            TextButton(onClick = onHomeClick) {
                Text("Home")
            }
        }
    })
}