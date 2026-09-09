package alic.malorodow.geoguessr_app.ui.streetviewtest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.StreetViewPanoramaView
import com.google.android.gms.maps.model.LatLng

/**
 * Isolierte Testansicht für Google Street View.
 *
 * Die Ansicht prüft unabhängig vom Spielablauf:
 * - Laden eines Panoramas
 * - Drehen und Zoomen
 * - Bewegung zwischen verbundenen Panoramaaufnahmen
 */
@Composable
fun StreetViewTestScreen(
    onBackClick: () -> Unit, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val panoramaView = remember {
        StreetViewPanoramaView(context).apply {
            onCreate(null)

            getStreetViewPanoramaAsync { panorama ->
                panorama.setPosition(TEST_LOCATION)
                panorama.isPanningGesturesEnabled = true
                panorama.isZoomGesturesEnabled = true
                panorama.isUserNavigationEnabled = true
                panorama.isStreetNamesEnabled = true
            }
        }
    }

    /**
     * Verbindet den Lebenszyklus der klassischen Android-View
     * mit dem Lebenszyklus des Compose-Screens.
     */
    DisposableEffect(lifecycleOwner, panoramaView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> panoramaView.onStart()
                Lifecycle.Event.ON_RESUME -> panoramaView.onResume()
                Lifecycle.Event.ON_PAUSE -> panoramaView.onPause()
                Lifecycle.Event.ON_STOP -> panoramaView.onStop()
                else -> { /* Keine Aktion für andere Events */
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            panoramaView.onResume()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            panoramaView.onPause()
            panoramaView.onStop()
            panoramaView.onDestroy()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Street-View-Testansicht", style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Drehe dich um, zoome und bewege dich durch die Umgebung."
        )

        AndroidView(
            factory = { panoramaView }, modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Button(
            onClick = onBackClick, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zurück zum Hauptmenü")
        }
    }
}

/**
 * Teststandort am Times Square in New York.
 */
private val TEST_LOCATION = LatLng(
    40.7580, -73.9855
)
