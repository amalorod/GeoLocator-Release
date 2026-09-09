package alic.malorodow.geoguessr_app.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import alic.malorodow.geoguessr_app.data.statistics.LeaderboardEntry
import alic.malorodow.geoguessr_app.data.statistics.StatisticsRepository
import alic.malorodow.geoguessr_app.domain.model.statistics.MatchStatistic
import alic.malorodow.geoguessr_app.domain.statistics.LifetimeStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Stellt Lebenszeit-Statistiken, zuletzt gespielte Partien und das globale
 * Leaderboard für [LifetimeStatisticsScreen] bereit.
 *
 * [statistics] und [recentMatches] werden bereits beim Erstellen dieses
 * ViewModels über [StatisticsRepository] beobachtet (keine explizite
 * Lade-Methode nötig). [topPlayers] hingegen bleibt bewusst leer, bis
 * [loadLeaderboard] aktiv aufgerufen wird – das Leaderboard wird nur
 * abgefragt, wenn der Nutzer es tatsächlich öffnet, um unnötige
 * Firebase-Requests zu vermeiden (insbesondere für Gäste, die das
 * Leaderboard laut UI ohnehin nicht einsehen können).
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    val statistics: StateFlow<LifetimeStatistics> = statisticsRepository.statistics
    val recentMatches: StateFlow<List<MatchStatistic>> = statisticsRepository.recentMatches
    val topPlayers: StateFlow<List<LeaderboardEntry>> = statisticsRepository.topPlayers

    /**
     * Lädt das globale Leaderboard einmalig nach. Der Guard verhindert
     * einen erneuten Netzwerk-Request, falls das Leaderboard beim
     * aktuellen Screen-Besuch bereits erfolgreich geladen wurde (z. B.
     * bei mehrfachem Öffnen/Schließen des Leaderboard-Dialogs).
     */
    fun loadLeaderboard() {
        if (topPlayers.value.isNotEmpty()) return

        viewModelScope.launch {
            statisticsRepository.loadLeaderboard()
        }
    }
}