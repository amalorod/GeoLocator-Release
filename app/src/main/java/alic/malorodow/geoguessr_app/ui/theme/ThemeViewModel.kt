package alic.malorodow.geoguessr_app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import alic.malorodow.geoguessr_app.data.datastore.ThemeDataStoreRepository
import alic.malorodow.geoguessr_app.data.datastore.ThemeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * App-weiter State-Holder für das aktuell aktive Farbschema.
 *
 * Da Theme-Änderungen sowohl im HomeScreen als auch im GameScreen ausgelöst
 * werden können, wäre ein screen-lokales ViewModel hier architektonisch
 * falsch: Beide Screens sowie [alic.malorodow.geoguessr_app.MainActivity]
 * (welche das Theme auf den gesamten Compose-Baum anwendet) benötigen den
 * gleichen State. Dieses ViewModel wird daher einmalig auf Activity-Ebene
 * über hiltViewModel() bezogen und als Parameter an untergeordnete Screens
 * weitergegeben, statt in jedem Screen erneut instanziiert zu werden.
 *
 * Persistenz übernimmt ausschließlich [ThemeDataStoreRepository]; dieses
 * ViewModel wandelt den kalten Repository-Flow lediglich in einen für
 * Compose optimierten, "warmen" [StateFlow] um.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val repository: ThemeDataStoreRepository
) : ViewModel() {

    /**
     * Aktuelle Theme-Einstellungen als StateFlow. WhileSubscribed(5000) hält
     * den zugrunde liegenden Flow noch 5 Sekunden nach Wegfall des letzten
     * Collectors aktiv, um kurze Konfigurationsänderungen (z. B. Rotation)
     * ohne unnötigen Neustart der DataStore-Collection zu überbrücken.
     */
    val themeSettings: StateFlow<ThemeSettings> = repository.themeSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeSettings()
        )

    /**
     * Setzt ein festes Farbschema (Hell/Dunkel/Beige/Blau/Rosé) und
     * deaktiviert dabei automatisch die dynamische Systemfarbe.
     */
    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    /**
     * Schaltet die Verwendung der Material-You-Systemfarbe (Android 12+) um.
     */
    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDynamicColorEnabled(enabled)
        }
    }
}