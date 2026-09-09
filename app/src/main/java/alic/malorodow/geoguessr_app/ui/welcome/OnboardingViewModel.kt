package alic.malorodow.geoguessr_app.ui.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import alic.malorodow.geoguessr_app.data.datastore.OnboardingDataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import alic.malorodow.geoguessr_app.navigation.GeoGuessrNavHost

/**
 * Bindet [OnboardingDataStoreRepository] an [GeoGuessrNavHost], um beim
 * App-Start zu entscheiden, ob [WelcomeScreen] gezeigt werden muss.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingDataStoreRepository: OnboardingDataStoreRepository
) : ViewModel() {


    val hasSeenTutorial: StateFlow<Boolean?> = onboardingDataStoreRepository.hasSeenTutorial
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
            initialValue = null
        )

    fun markTutorialAsSeen() {
        viewModelScope.launch {
            onboardingDataStoreRepository.markTutorialAsSeen()
        }
    }
}