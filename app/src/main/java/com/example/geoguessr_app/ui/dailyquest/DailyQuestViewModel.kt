package com.example.geoguessr_app.ui.dailyquest

import androidx.lifecycle.ViewModel
import com.example.geoguessr_app.data.dailyquest.DailyQuestRepository
import com.example.geoguessr_app.domain.model.dailyquest.DailyQuest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DailyQuestViewModel @Inject constructor(
    private val dailyQuestRepository: DailyQuestRepository
) : ViewModel() {

    val quests: StateFlow<List<DailyQuest>> = dailyQuestRepository.quests
}
