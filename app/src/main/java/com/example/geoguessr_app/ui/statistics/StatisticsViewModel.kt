package com.example.geoguessr_app.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoguessr_app.data.statistics.LeaderboardEntry
import com.example.geoguessr_app.data.statistics.StatisticsRepository
import com.example.geoguessr_app.domain.model.statistics.MatchStatistic
import com.example.geoguessr_app.domain.statistics.LifetimeStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    val statistics: StateFlow<LifetimeStatistics> = statisticsRepository.statistics
    val recentMatches: StateFlow<List<MatchStatistic>> = statisticsRepository.recentMatches
    val topPlayers: StateFlow<List<LeaderboardEntry>> = statisticsRepository.topPlayers

    fun loadLeaderboard() {
        viewModelScope.launch {
            statisticsRepository.loadLeaderboard()
        }
    }
}
