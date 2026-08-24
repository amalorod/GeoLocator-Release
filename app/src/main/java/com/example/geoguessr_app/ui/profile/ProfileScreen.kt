package com.example.geoguessr_app.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import com.example.geoguessr_app.data.profile.ProfileRepository
import com.example.geoguessr_app.domain.model.profile.PlayerProfile
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    profile: PlayerProfile,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val isGuest = profile.playerId.isEmpty() || profile.playerName == "Spieler"
    
    var showLoginModal by remember { mutableStateOf(false) }
    var loginUsername by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var modalError by remember { mutableStateOf<String?>(null) }

    // Launcher für Galerie
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                ProfileRepository.uploadProfilePicture(it)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Custom Header
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
                Spacer(modifier = Modifier.width(8.dp))
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

            // Profilbild mit Klick-Funktion
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(enabled = !isGuest) {
                        galleryLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                if (profile.profileImageUrl != null) {
                    AsyncImage(
                        model = profile.profileImageUrl,
                        contentDescription = "Profilbild",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = if (profile.playerName.isNotEmpty()) profile.playerName.take(1).uppercase() else "?",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Overlay für Kamera-Icon wenn nicht Gast
                if (!isGuest) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text("Ändern", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isGuest) "Gast-Account" else profile.playerName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            if (!isGuest) {
                Text(
                    text = "UID: ${profile.playerId.take(12)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isGuest) {
                Button(
                    onClick = { showLoginModal = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Einloggen / Profil laden", style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Logge dich ein, um deine Statistiken zu speichern und ein Profilbild hochzuladen.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 48.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Account-Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        DetailRow(icon = Icons.Default.Person, label = "Nutzername", value = profile.playerName)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(icon = Icons.Default.DateRange, label = "Mitglied seit", value = formatDate(profile.createdAt))
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                TextButton(onClick = { /* Logout Logik? */ }) {
                    Text("Abmelden", color = MaterialTheme.colorScheme.error)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Login / Register Modal
    if (showLoginModal) {
        AlertDialog(
            onDismissRequest = { if (!isProcessing) showLoginModal = false },
            title = { Text("Willkommen zurück!") },
            text = {
                Column {
                    Text("Gib deinen Nutzernamen ein. Wenn er existiert, wird dein Profil geladen. Wenn nicht, wird ein neues erstellt.")
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
                    if (modalError != null) {
                        Text(modalError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (loginUsername.length >= 3) {
                            isProcessing = true
                            scope.launch {
                                // Erst versuchen einzuloggen
                                val found = ProfileRepository.loginWithUsername(loginUsername)
                                if (!found) {
                                    // Wenn nicht gefunden, neues Profil erstellen
                                    ProfileRepository.createAndLogin(loginUsername)
                                }
                                isProcessing = false
                                showLoginModal = false
                                loginUsername = ""
                            }
                        } else {
                            modalError = "Mindestens 3 Zeichen benötigt."
                        }
                    },
                    enabled = !isProcessing && loginUsername.length >= 3
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
                    onClick = { showLoginModal = false },
                    enabled = !isProcessing
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "Unbekannt"
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
