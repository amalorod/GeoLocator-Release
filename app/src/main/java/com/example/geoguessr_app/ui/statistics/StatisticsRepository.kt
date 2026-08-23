package com.example.geoguessr_app.data.statistics

import com.example.geoguessr_app.domain.model.statistics.LifetimeStatistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StatisticsRepository {

    private val _statistics =
        MutableStateFlow(LifetimeStatistics())

    val statistics: StateFlow<LifetimeStatistics> =
        _statistics.asStateFlow()

    fun updateStatistics(
        newStatistics: LifetimeStatistics
    ) {
        _statistics.value = newStatistics
    }
}