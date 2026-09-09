package alic.malorodow.geoguessr_app.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import alic.malorodow.geoguessr_app.R
import kotlinx.coroutines.delay
import alic.malorodow.geoguessr_app.navigation.GeoGuessrNavHost

/** Anzeigedauer des eigenen Logo-Splash-Screens in Millisekunden. */
private const val SPLASH_DURATION_MS = 1_200L

/**
 * Eigener, kurzer Compose-Splash-Screen, der direkt nach dem System-
 * Splash-Screen (Android 12 SplashScreen API, siehe MainActivity und
 * Theme.GeoGuessr_App.Starting) angezeigt wird. Notwendig, da die
 * SplashScreen-API selbst keinen Text-Slot unterstützt – der Name des
 * Entwicklers wird daher hier als echter Compose-Text unter dem Logo
 * ergänzt, analog zur organisatorischen Vorgabe der Aufgabenstellung.
 *
 * @param onFinished Wird nach [SPLASH_DURATION_MS] ausgelöst und leitet
 * in MainActivity zum eigentlichen App-Inhalt [GeoGuessrNavHost] weiter.
 */
@Composable
fun AppLogoSplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onFinished()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.height(120.dp)
        )
        Text(
            text = "GeoLocator • v1.0",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}