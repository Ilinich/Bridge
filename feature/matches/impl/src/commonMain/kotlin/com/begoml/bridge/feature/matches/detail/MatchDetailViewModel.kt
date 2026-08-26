package com.begoml.bridge.feature.matches.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bridge.feature.matches.impl.generated.resources.Res
import bridge.feature.matches.impl.generated.resources.fixture_score
import bridge.feature.matches.impl.generated.resources.fixture_versus
import bridge.feature.matches.impl.generated.resources.match_kickoff
import bridge.feature.matches.impl.generated.resources.match_not_found
import bridge.feature.matches.impl.generated.resources.match_title
import com.begoml.bridge.core.data.model.SeasonMatch
import com.begoml.bridge.core.data.repository.MatchRepository
import com.begoml.bridge.feature.matches.formatKickoff
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateUp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString

data class MatchDetailLabels(
    val title: String,
    val notFound: String,
    val kickoff: String,
)

data class MatchDetailUi(
    val homeName: String,
    val homeCode: String,
    val awayName: String,
    val awayCode: String,
    val scoreline: String,
    val kickoff: String,
)

data class MatchDetailUiState(
    val match: MatchDetailUi? = null,
    val labels: MatchDetailLabels? = null,
)

internal class MatchDetailViewModel(
    matchId: String,
    matchRepository: MatchRepository,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val labels = MutableStateFlow<MatchDetailLabels?>(null)

    val state: StateFlow<MatchDetailUiState> =
        combine(matchRepository.match(matchId), labels) { match, resolved ->
            MatchDetailUiState(
                match = match?.let { withContext(ioDispatcher) { it.toUi() } },
                labels = resolved,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, MatchDetailUiState())

    init {
        viewModelScope.launch { labels.value = withContext(ioDispatcher) { readLabels() } }
    }

    fun onBack() {
        router.navigateUp()
    }

    private suspend fun SeasonMatch.toUi() = MatchDetailUi(
        homeName = home.name,
        homeCode = home.code,
        awayName = away.name,
        awayCode = away.code,
        scoreline = score
            ?.let { getString(Res.string.fixture_score, it.home, it.away) }
            ?: getString(Res.string.fixture_versus),
        kickoff = kickoff.formatKickoff(),
    )

    private suspend fun readLabels() = MatchDetailLabels(
        title = getString(Res.string.match_title),
        notFound = getString(Res.string.match_not_found),
        kickoff = getString(Res.string.match_kickoff),
    )
}
