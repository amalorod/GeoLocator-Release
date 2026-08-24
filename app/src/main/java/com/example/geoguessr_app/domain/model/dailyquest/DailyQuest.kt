package com.example.geoguessr_app.domain.model.dailyquest

data class DailyQuest(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val icon: String = "",
    val completed: Boolean = false,
    val progress: Int = 0,
    val target: Int = 1
)
