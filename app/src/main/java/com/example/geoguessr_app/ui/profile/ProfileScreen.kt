package com.example.geoguessr_app.ui.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.geoguessr_app.domain.model.profile.PlayerProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Profil-Übersicht mit zwei grundverschiedenen Zuständen: Gast (kein
 * gespeicherter Fortschritt, Login-Aufforderung) und eingeloggter Nutzer
 * (Account-Details, Profilbild-Upload, Logout).
 *
 * @param profile Aktuell aktives Profil; unterscheidet über [isGuest()]
 * zwischen Gast- und Account-Ansicht.
 * @param onBackClick Navigiert zurück zum Startbildschirm.
 */
@Composable
fun ProfileScreen(
    profile: PlayerProfile,
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()

    // Erkennt einen Gast-Zustand, indem bewusst zwischen Vorhandensein eines Profils unterschieden wird
    val isGuest = profile.isGuest

    var showLoginModal by remember { mutableStateOf(false) }
    var loginUsername by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var modalError by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadPicture(it) }
    }

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
                    text = "Profil",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = !isGuest) {
                        try {
                            galleryLauncher.launch("image/*")
                        } catch (e: Exception) {
                            Log.e("PROFILE", "Galerie Fehler", e)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!isGuest && profile.profileImageUrl != null) {
                    AsyncImage(
                        model = profile.profileImageUrl,
                        contentDescription = "Profilbild",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = {
                            Log.e(
                                "PROFILE",
                                "Fehler beim Laden des Bildes: ${it.result.throwable.message}"
                            )
                        }
                    )
                } else {
                    Text(
                        text = if (!isGuest && profile.playerName.isNotEmpty()) {
                            profile.playerName.take(1).uppercase()
                        } else {
                            "?"
                        },
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isGuest) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            "ÄNDERN",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isGuest) "Gast" else profile.playerName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )



            Spacer(modifier = Modifier.height(32.dp))

            if (isGuest) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Melde dich an, um deinen Fortschritt zu speichern.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showLoginModal = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Einloggen / Profil laden")
                        }
                    }
                }
            } else {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Account-Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow(label = "Nutzername", value = profile.playerName, emoji = "👤")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        DetailRow(
                            label = "Dabei seit",
                            value = formatDate(profile.createdAt),
                            emoji = "📅"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(onClick = { viewModel.logout() }) {
                    Text("Vom Gerät abmelden", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showLoginModal) {
        AlertDialog(
            onDismissRequest = { if (!isProcessing) showLoginModal = false },
            title = { Text("Spieler-Login") },
            text = {
                Column {
                    Text("Gib deinen Namen ein. Falls er existiert, laden wir deine Statistiken.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = loginUsername,
                        onValueChange = {
                            loginUsername = it
                            modalError = null
                        },
                        label = { Text("Nutzername") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isProcessing
                    )
                    modalError?.let { error ->
                        Text(
                            error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (loginUsername.trim().length >= 3) {
                            isProcessing = true
                            viewModel.login(
                                userName = loginUsername.trim(),
                                onSuccess = {
                                    isProcessing = false
                                    showLoginModal = false
                                    loginUsername = ""
                                },
                                onError = {
                                    // Kein bestehender Account mit diesem Namen
                                    // gefunden -> automatischer Fallback auf
                                    // Account-Erstellung statt einer separaten
                                    // Fehlermeldung, die den Nutzer zu einem
                                    // erneuten, expliziten Klick zwingen würde.
                                    viewModel.createAccount(loginUsername.trim()) {
                                        isProcessing = false
                                        showLoginModal = false
                                        loginUsername = ""
                                    }
                                }
                            )
                        } else {
                            modalError = "Mindestens 3 Zeichen erforderlich."
                        }
                    },
                    enabled = !isProcessing && loginUsername.trim().length >= 3
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Bestätigen")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLoginModal = false
                        loginUsername = ""
                        modalError = null
                    },
                    enabled = !isProcessing
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

/** Einzelne Zeile im "Account-Details"-Bereich mit Emoji-Icon, Label und Wert. */
@Composable
private fun DetailRow(label: String, value: String, emoji: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** Formatiert einen Unix-Timestamp als deutsches Datum (TT.MM.JJJJ). */
private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return "Heute"
    return try {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
    } catch (ignore: Exception) {
        "Unbekannt"
    }
}