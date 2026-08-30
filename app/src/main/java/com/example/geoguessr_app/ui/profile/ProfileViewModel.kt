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

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val statisticsRepository: StatisticsRepository,
    private val dailyQuestRepository: DailyQuestRepository
) : ViewModel() {

    val profile: StateFlow<PlayerProfile?> = profileRepository.profile
    val isLoaded: StateFlow<Boolean> = profileRepository.isLoaded

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
                // Für Gäste wird das Leaderboard durch loadLocalStatistics()
                // nicht mitgeladen (dieser Pfad lädt nur die eigenen,
                // lokalen Werte, kein globales Ranking). Es wird deshalb
                // hier explizit nachgeladen, damit auch Gäste das globale
                // Leaderboard sehen können, selbst wenn sie selbst nicht
                // darin vorkommen.
                statisticsRepository.loadLocalStatistics()
                dailyQuestRepository.loadQuests()
                statisticsRepository.loadLeaderboard()
            }
        }
    }

    fun login(userName: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = profileRepository.loginWithUsername(userName)
            if (success) {
                val uid = profileRepository.profile.value?.playerId
                statisticsRepository.loadStatistics(uid)
                dailyQuestRepository.loadQuests()
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun createAccount(userName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val success = profileRepository.createAndLogin(userName)
            if (success) {
                val uid = profileRepository.profile.value?.playerId
                statisticsRepository.loadStatistics(uid)
                dailyQuestRepository.loadQuests()
                onSuccess()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            profileRepository.logout()
            statisticsRepository.loadLocalStatistics()
            dailyQuestRepository.loadQuests()
        }
    }

    fun uploadPicture(uri: Uri) {
        viewModelScope.launch {
            profileRepository.uploadProfilePicture(uri)
        }
    }
}
