package com.example.geoguessr_app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geoguessr_app.R
import com.example.geoguessr_app.ui.game.GameMode
import com.example.geoguessr_app.ui.theme.AppThemeMode
import kotlinx.coroutines.delay

/**
 * Bildwechsel-Intervall für den rotierenden Hintergrund-Header in Millisekunden.
 * Als benannte Konstante statt Magic Number, um die Bedeutung des Werts
 * ohne Blick in [LaunchedEffect] zu erschließen und ihn zentral änderbar zu machen.
 */
private const val HEADER_ROTATION_INTERVAL_MS = 4_000L

/**
 * Bildschirmbreite ab der das Layout als Tablet behandelt und der Kartenausschnitt
 * vertikal versetzt dargestellt wird (Breakpoint nach Material-Design-Konvention).
 */
private const val TABLET_BREAKPOINT_DP = 600

/**
 * Rotierende Header-Bilder für den Startbildschirm. Als Top-Level-Konstante statt
 * lokaler Liste innerhalb des Composables definiert, damit die Liste nicht bei
 * jeder Recomposition von [HomeScreen] neu alloziert wird.
 */
private val HEADER_IMAGES = listOf(
    R.drawable.home_header_berlin,
    R.drawable.home_header_france,
    R.drawable.home_header_italy
)

/**
 * Hauptbildschirm der App (Startseite). Zeigt eine rotierende Bildergalerie
 * europäischer Städte, den Einstieg in den Spielmodus-Dialog, Kurzzugriffe auf
 * Profil/Statistik/Daily-Quest sowie den Einstiegspunkt für die Theme-Auswahl.
 *
 * Der Screen ist bewusst zustandslos bezüglich aller fachlichen Daten
 * (Spielstand, Profilstatus, Theme) – diese werden ausschließlich über
 * Parameter hereingereicht und Änderungen über Callbacks nach oben an
 * [com.example.geoguessr_app.MainActivity] bzw. das zuständige ViewModel
 * gemeldet. Lokal verwaltet der Screen nur rein visuellen UI-Zustand
 * (aktueller Bildindex, Sichtbarkeit des Modus-Dialogs).
 *
 * @param selectedGameMode Aktuell gewählter Spielmodus, wird im Modus-Button angezeigt.
 * @param onGameModeSelected Callback bei Auswahl eines neuen Spielmodus im Dialog.
 * @param currentThemeName Anzeigename des aktuell aktiven Farbschemas (an [EuropeMenuMap] weitergereicht).
 * @param isProfileSetup Ob bereits ein Spielerprofil angelegt wurde; steuert die Statusfarbe des Profil-Buttons.
 * @param onThemeClick Callback, der die Theme-Auswahl öffnet (UI dafür liegt in [EuropeMenuMap]).
 * @param onStartGameClick Startet ein neues Spiel im aktuell gewählten Modus.
 * @param onTutorialClick Öffnet das Tutorial.
 * @param hasActiveGame Ob eine pausierte/laufende Partie fortgesetzt werden kann.
 * @param onResumeGameClick Setzt eine laufende Partie fort.
 * @param onExitAppClick Beendet die App vollständig.
 * @param onProfileClick Öffnet den Profil-Screen.
 * @param onStatisticsClick Öffnet den Statistik-Screen.
 * @param onDailyQuestClick Öffnet den Daily-Quest-Screen.
 */
@Composable
fun HomeScreen(
    selectedGameMode: GameMode,
    onGameModeSelected: (GameMode) -> Unit,
    currentTheme: AppThemeMode,
    currentDynamicColorEnabled: Boolean,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDynamicColorToggled: (Boolean) -> Unit,
    isProfileSetup: Boolean,
    onStartGameClick: () -> Unit,
    onTutorialClick: () -> Unit,
    hasActiveGame: Boolean,
    onResumeGameClick: () -> Unit,
    onExitAppClick: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onDailyQuestClick: () -> Unit,
) {
    // Rein visueller UI-Zustand: Sichtbarkeit des Spielmodus-Auswahldialogs.
    var isModeDialogVisible by remember { mutableStateOf(false) }

    // Breakpoint-Entscheidung für responsives Layout (Smartphone vs. Tablet).
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= TABLET_BREAKPOINT_DP

    // Index des aktuell sichtbaren Header-Bilds. rememberSaveable, damit die
    // Rotation nach einer Konfigurationsänderung (z. B. Bildschirmdrehung)
    // nicht wieder bei Bild 0 beginnt.
    var currentImageIndex by rememberSaveable { mutableIntStateOf(0) }

    // Wechselt automatisch alle HEADER_ROTATION_INTERVAL_MS zum nächsten Bild.
    // Der Key currentImageIndex sorgt dafür, dass nach jedem Wechsel ein neuer
    // Coroutine-Zyklus mit erneuter Wartezeit gestartet wird.
    LaunchedEffect(currentImageIndex) {
        delay(HEADER_ROTATION_INTERVAL_MS)
        currentImageIndex = (currentImageIndex + 1) % HEADER_IMAGES.size
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Begrenzt die Inhaltsbreite auf 700dp, damit das Layout auf
            // Tablets nicht unnatürlich in die Breite gezogen wird.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 700.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    RotatingTornHeader(
                        imageId = HEADER_IMAGES[currentImageIndex],
                        height = 230.dp,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 42.dp, start = 20.dp, end = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GeoGuessr",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            // onPrimaryContainer statt onSurface, um ausreichenden
                            // Kontrast über dem Hintergrundbild sicherzustellen.
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "Entdecke die Welt und errate den Standort!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    EuropeMenuMap(
                        backgroundImageId = HEADER_IMAGES[currentImageIndex],
                        hasActiveGame = hasActiveGame,
                        currentTheme = currentTheme,
                        currentDynamicColorEnabled = currentDynamicColorEnabled,
                        onThemeSelected = onThemeSelected,
                        onDynamicColorToggled = onDynamicColorToggled,
                        onStartGameClick = onStartGameClick,
                        onResumeGameClick = onResumeGameClick,
                        onTutorialClick = onTutorialClick,
                        onExitAppClick = onExitAppClick,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = if (isTablet) 40.dp else 0.dp)
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                    )

                    Button(
                        onClick = { isModeDialogVisible = true },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 52.dp)
                            .width(350.dp)
                            .height(58.dp),
                        shape = CompassBannerShape(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "MODUS: ${selectedGameMode.displayName.uppercase()}",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 130.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        HomeShortcutButton(
                            icon = "👤",
                            onClick = onProfileClick,
                            // Grün/Rot signalisiert, ob bereits ein Profil
                            // angelegt wurde – roter Indikator als Aufforderung.
                            statusColor = if (isProfileSetup) Color.Green else Color.Red
                        )

                        HomeShortcutButton(icon = "📊", onClick = onStatisticsClick)
                        HomeShortcutButton(icon = "📅", onClick = onDailyQuestClick)
                    }

                    Text(
                        text = "Entwickelt von: Alic Malorodow",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 15.dp)
                    )
                }
            }
        }

        if (isModeDialogVisible) {
            GameModeDialog(
                selectedMode = selectedGameMode,
                onModeSelected = onGameModeSelected,
                onDismiss = { isModeDialogVisible = false }
            )
        }
    }
}

/**
 * Individuelle Wegweiser-Form mit spitzen Enden auf beiden Seiten, angelehnt an
 * eine Kompass-/Bannergrafik. Wird ausschließlich als Button-Shape des
 * Spielmodus-Buttons in [HomeScreen] verwendet.
 */
private class CompassBannerShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(size.width * 0.08f, 0f)
            lineTo(size.width * 0.92f, 0f)
            lineTo(size.width, size.height * 0.50f)
            lineTo(size.width * 0.92f, size.height)
            lineTo(size.width * 0.08f, size.height)
            lineTo(0f, size.height * 0.50f)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Runder Kurzzugriffs-Button mit Emoji-Icon, optional mit farbigem Status-Punkt
 * oben rechts (z. B. zur Anzeige eines fehlenden Profils).
 *
 * @param icon Emoji, das als Buttoninhalt angezeigt wird.
 * @param onClick Klick-Callback.
 * @param statusColor Farbe des Status-Indikators; null blendet ihn aus.
 */
@Composable
private fun HomeShortcutButton(
    icon: String,
    onClick: () -> Unit,
    statusColor: Color? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(72.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = icon, fontSize = 28.sp, textAlign = TextAlign.Center)
            }
        }

        if (statusColor != null) {
            Surface(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp),
                shape = CircleShape,
                color = statusColor,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
            ) {}
        }
    }
}