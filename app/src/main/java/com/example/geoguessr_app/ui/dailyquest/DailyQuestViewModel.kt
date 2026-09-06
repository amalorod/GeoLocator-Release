package com.example.geoguessr_app.ui.dailyquest

import androidx.lifecycle.ViewModel
import com.example.geoguessr_app.data.dailyquest.DailyQuestRepository
import com.example.geoguessr_app.domain.model.dailyquest.DailyQuest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import com.example.geoguessr_app.ui.game.GameViewModel

/**
 * Reicht den aktuellen Stand aller Daily Quests unverändert aus dem
 * Repository an [DailyQuestScreen] weiter.
 *
 * Bewusst ohne eigenen UiState/MutableStateFlow: Da hier keine
 * Transformation oder zusätzliche UI-Logik nötig ist (im Gegensatz z. B.
 * zu [GameViewModel]), wird der Repository-Flow direkt durchgereicht,
 * statt ihn zu duplizieren.
 */
@HiltViewModel
class DailyQuestViewModel @Inject constructor(
    private val dailyQuestRepository: DailyQuestRepository
) : ViewModel() {

    val quests: StateFlow<List<DailyQuest>> = dailyQuestRepository.quests
}