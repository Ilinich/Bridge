package com.begoml.bridge.feature.matches.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bridge.feature.matches.impl.generated.resources.Res
import bridge.feature.matches.impl.generated.resources.fixture_score
import bridge.feature.matches.impl.generated.resources.fixture_versus
import bridge.feature.matches.impl.generated.resources.match_back
import bridge.feature.matches.impl.generated.resources.match_kickoff
import bridge.feature.matches.impl.generated.resources.match_not_found
import bridge.feature.matches.impl.generated.resources.match_title
import com.begoml.bridge.core.data.model.Loadable
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

/** How long a state flow outlives its last collector, so a configuration change does not refetch. */
internal const val SubscriptionTimeoutMillis = 5_000L

data class MatchDetailLabels(
    val title: String,
    val back: String,
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
    /** True until both the fixture and the labels have answered; absent is not the same as loading. */
    val isLoading: Boolean = true,
)

internal class MatchDetailViewModel(
    matchId: String,
    matchRepository: MatchRepository,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val labels = MutableStateFlow<MatchDetailLabels?>(null)

    val state: StateFlow<MatchDetailUiState> =
        combine(matchRepository.match(matchId), labels) { loadable, resolved ->
            val match = (loadable as? Loadable.Content)?.value
            MatchDetailUiState(
                match = match?.let { withContext(ioDispatcher) { it.toUi() } },
                labels = resolved,
                isLoading = loadable is Loadable.Loading || resolved == null,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SubscriptionTimeoutMillis), MatchDetailUiState())

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
        back = getString(Res.string.match_back),
        notFound = getString(Res.string.match_not_found),
        kickoff = getString(Res.string.match_kickoff),
    )
}
