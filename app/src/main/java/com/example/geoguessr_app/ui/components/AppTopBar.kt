package com.example.geoguessr_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.geoguessr_app.ui.theme.AppThemeMode

/**
 * Gemeinsame Steuerleiste für den Spielbildschirm.
 */
@Composable
fun AppTopBar(
    currentThemeName: String,
    currentTheme: AppThemeMode,
    isPauseEnabled: Boolean,
    onThemeSelected: (AppThemeMode) -> Unit,
    onHomeClick: () -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isThemeMenuExpanded by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onHomeClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Home")
        }

        Button(
            onClick = onPauseClick,
            enabled = isPauseEnabled,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Pause")
        }

        Box(
            modifier = Modifier.weight(1f)
        ) {
            Button(
                onClick = {
                    isThemeMenuExpanded = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(
                                color = currentTheme.previewColor(),
                                shape = CircleShape
                            )
                    )

                    Text("Design")
                }
            }

            DropdownMenu(
                expanded = isThemeMenuExpanded,
                onDismissRequest = {
                    isThemeMenuExpanded = false
                }
            ) {
                AppThemeMode.entries.forEach { theme ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(
                                            color = theme.previewColor(),
                                            shape = CircleShape
                                        )
                                )

                                Text(
                                    text = theme.displayName,
                                    fontWeight = if (theme == currentTheme) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            }
                        },
                        onClick = {
                            onThemeSelected(theme)
                            isThemeMenuExpanded = false
                        },
                        modifier = Modifier.defaultMinSize(
                            minWidth = 190.dp,
                            minHeight = 56.dp
                        )
                    )
                }
            }
        }
    }
}

private fun AppThemeMode.previewColor(): Color {
    return when (this) {
        AppThemeMode.LIGHT -> Color(0xFFF2EFFF)
        AppThemeMode.DARK -> Color(0xFF29252E)
        AppThemeMode.BEIGE -> Color(0xFFD8C3A5)
        AppThemeMode.BLUE -> Color(0xFF42A5F5)
        AppThemeMode.ROSE -> Color(0xFFD987A5)
    }
}
