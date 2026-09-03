package com.example.geoguessr_app.ui.maptest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import java.util.Locale

/**
 * Isolierte Testansicht für Google Maps.
 *
 * Die Ansicht gehört noch nicht zum eigentlichen Spielablauf.
 * Sie prüft ausschließlich, ob Google Maps geladen wird und eine
 * Position durch Berühren der Karte ausgewählt werden kann.
 */
@Composable
fun MapTestScreen(
    onBackClick: () -> Unit, modifier: Modifier = Modifier
) {
    var selectedPosition by remember {
        mutableStateOf<LatLng?>(null)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            WORLD_CENTER, INITIAL_ZOOM
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Karten-Testansicht", style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Tippe auf einen beliebigen Ort der Karte.", textAlign = TextAlign.Center
        )

        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            cameraPositionState = cameraPositionState,
            onMapClick = { clickedPosition ->
                selectedPosition = clickedPosition
            }) {
            selectedPosition?.let { position ->
                Marker(
                    state = rememberUpdatedMarkerState(
                        position = position
                    ), title = "Ausgewählter Tipp", snippet = formatCoordinates(position)
                )
            }
        }

        Text(
            text = selectedPosition?.let(::formatCoordinates) ?: "Noch keine Position ausgewählt",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onBackClick, modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Zurück")
        }
    }
}

/**
 * Formatiert geografische Koordinaten mit vier Nachkommastellen.
 */
private fun formatCoordinates(position: LatLng): String {
    return String.format(
        Locale.GERMANY, "Breitengrad: %.4f, Längengrad: %.4f", position.latitude, position.longitude
    )
}

private val WORLD_CENTER = LatLng(
    20.0, 0.0
)

private const val INITIAL_ZOOM = 1.5f