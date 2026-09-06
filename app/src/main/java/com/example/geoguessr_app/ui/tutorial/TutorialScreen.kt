package com.example.geoguessr_app.ui.tutorial

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Klick-Galerie, die die grundlegende Spielsteuerung anhand von
 * [tutorialSteps] Schritt für Schritt erklärt.
 *
 * Wird in zwei Kontexten verwendet (siehe [isWelcomeFlow]):
 * - Normaler Aufruf über EuropeMenuMap: letzter Schritt zeigt "Fertig",
 *   [onFinish] navigiert zurück (popBackStack).
 * - Erster App-Start über [WelcomeScreen]: letzter Schritt zeigt
 *   "Home" statt "Weiter", [onFinish] navigiert direkt zum Home-Screen
 *   und markiert das Tutorial zusätzlich als gesehen (siehe
 *   [GeoGuessrNavHost]).
 *
 * @param isWelcomeFlow Steuert nur die Beschriftung/Sichtbarkeit des
 * letzten Buttons, nicht die Galerie-Logik selbst.
 * @param onFinish Wird ausgelöst, wenn der letzte Schritt bestätigt wird.
 * @param onBackClick Verlässt das Tutorial vorzeitig; im Welcome-Flow
 * bewusst nicht angeboten (siehe Aufrufstelle), da der Nutzer das
 * Tutorial beim ersten Start vollständig durchlaufen soll.
 */
@Composable
fun TutorialScreen(
    onFinish: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    isWelcomeFlow: Boolean = false,
    modifier: Modifier = Modifier
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = tutorialSteps[currentStepIndex]
    val isLastStep = currentStepIndex == tutorialSteps.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (onBackClick != null) {
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
                        text = "Anleitung",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Schritt ${currentStepIndex + 1} von ${tutorialSteps.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = currentStep.titleRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            TutorialImagePlaceholder(
                imageRes = currentStep.imageRes,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = currentStep.descriptionRes),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { currentStepIndex-- },
                    enabled = currentStepIndex > 0
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Zurück")
                }

                if (isLastStep) {
                    Button(
                        onClick = onFinish,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isWelcomeFlow) "Home" else "Fertig")
                    }
                } else {
                    Button(onClick = { currentStepIndex++ }) {
                        Text("Weiter")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

/**
 * Zeigt entweder den echten Screenshot ([imageRes]) oder, solange dieser
 * noch nicht existiert, einen gestrichelt umrandeten Platzhalter mit
 * Hinweistext – so bleibt die Galerie bereits jetzt vollständig
 * durchklickbar, die Bilder können später ohne Strukturänderung ergänzt werden.
 */
@Composable
private fun TutorialImagePlaceholder(
    imageRes: Int?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageRes != null) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = "📷 Screenshot folgt",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}