package com.example.geoguessr_app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Seitlich angeheftete, ausfahrbare Hinweisleiste.
 *
 * Der Zustand wird pro Runde neu erzeugt, sodass der Hinweis beim
 * Rundenwechsel automatisch wieder eingeklappt ist.
 */
@Composable
fun HintPanel(
    hint: String,
    roundNumber: Int,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember(roundNumber) {
        mutableStateOf(false)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                isExpanded = !isExpanded
            },
            modifier = Modifier.defaultMinSize(
                minWidth = 52.dp,
                minHeight = 64.dp
            )
        ) {
            Text(
                text = "?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandHorizontally(
                expandFrom = Alignment.Start
            ),
            exit = fadeOut() + shrinkHorizontally(
                shrinkTowards = Alignment.Start
            )
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(
                        min = 180.dp,
                        max = 280.dp
                    )
                    .shadow(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                tonalElevation = 6.dp
            ) {
                Text(
                    text = "Tipp: $hint",
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 16.dp
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
