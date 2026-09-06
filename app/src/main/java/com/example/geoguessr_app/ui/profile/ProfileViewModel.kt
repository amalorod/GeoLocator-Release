package com.example.geoguessr_app.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoguessr_app.data.dailyquest.DailyQuestRepository
import com.example.geoguessr_app.data.profile.ProfileRepository
import com.example.geoguessr_app.data.statistics.StatisticsRepository
import com.example.geoguessr_app.domain.model.profile.PlayerProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.geoguessr_app.navigation.GeoGuessrNavHost

/**
 * Verwaltet Profil-, Login- und Logout-Zustand sowie das Nachladen von
 * Statistiken/Daily Quests bei Profilwechseln.
 *
 * Folgt durchgängig einer "Verwerfen-beim-Login"-Strategie: Beim Wechsel von
 * Gast zu eingeloggtem Account werden zuerst alle lokal gespeicherten
 * Gast-Statistiken/-Quests verworfen ([StatisticsRepository.clearLocalStatistics],
 * [DailyQuestRepository.clearLocalQuests]), bevor die Server-Daten des nun
 * aktiven Accounts geladen werden. Somit vermischen sich Gast- und
 * Account-Fortschritt nicht.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val statisticsRepository: StatisticsRepository,
    private val dailyQuestRepository: DailyQuestRepository
) : ViewModel() {

    val profile: StateFlow<PlayerProfile?> = profileRepository.profile
    val isLoaded: StateFlow<Boolean> = profileRepository.isLoaded

    /**
     * Initialer Ladevorgang beim App-Start (siehe [GeoGuessrNavHost]).
     *
     * Lädt je nachdem, ob ein eingeloggtes Profil existiert, entweder die
     * Server-Statistiken des Nutzers oder die rein lokalen Gast-Statistiken.
     */
    fun loadInitialData() {
        viewModelScope.launch {
            profileRepository.loadProfile()
            val currentProfile = profileRepository.profile.value
            if (currentProfile != null) {
                // loadStatistics() ruft intern bereits loadLeaderboard() auf
                // (siehe StatisticsRepository), ein zusätzlicher Aufruf hier
                // wäre daher redundant.
                statisticsRepository.loadStatistics(currentProfile.playerId)
                dailyQuestRepository.loadQuests()
            } else {
                statisticsRepository.loadLocalStatistics()
                dailyQuestRepository.loadQuests()
                statisticsRepository.loadLeaderboard()
            }
        }
    }

    /**
     * Meldet einen existierenden Account per Nutzername an.
     *
     * @param onSuccess Aufgerufen bei erfolgreichem Login.
     * @param onError Aufgerufen, wenn kein Account mit diesem Namen existiert;
     * [ProfileScreen] nutzt dies, um automatisch [createAccount] als Fallback aufzurufen.
     */
    fun login(userName: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = profileRepository.loginWithUsername(userName)
            if (success) {
                val uid = profileRepository.profile.value?.playerId
                statisticsRepository.clearLocalStatistics()
                dailyQuestRepository.clearLocalQuests()

                statisticsRepository.loadStatistics(uid)
                dailyQuestRepository.loadQuests()
                onSuccess()
            } else {
                onError()
            }
        }
    }

    /** Legt einen neuen Account mit dem angegebenen Namen an und meldet ihn direkt an. */
    fun createAccount(userName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val success = profileRepository.createAndLogin(userName)
            if (success) {
                val uid = profileRepository.profile.value?.playerId
                statisticsRepository.clearLocalStatistics()
                dailyQuestRepository.clearLocalQuests()

                statisticsRepository.loadStatistics(uid)
                dailyQuestRepository.loadQuests()
                onSuccess()
            }
        }
    }

    /** Meldet den aktuellen Account ab und kehrt zum lokalen Gast-Fortschritt zurück. */
    fun logout() {
        viewModelScope.launch {
            profileRepository.logout()
            statisticsRepository.loadLocalStatistics()
            dailyQuestRepository.loadQuests()
        }
    }

    /** Lädt ein neues Profilbild aus der Galerie hoch. */
    fun uploadPicture(uri: Uri) {
        viewModelScope.launch {
            profileRepository.uploadProfilePicture(uri)
        }
    }
}