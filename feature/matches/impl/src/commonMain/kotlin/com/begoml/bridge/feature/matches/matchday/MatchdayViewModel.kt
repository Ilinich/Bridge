package com.begoml.bridge.feature.matches.matchday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bridge.feature.matches.impl.generated.resources.Res
import bridge.feature.matches.impl.generated.resources.fixture_score
import bridge.feature.matches.impl.generated.resources.fixture_teams
import bridge.feature.matches.impl.generated.resources.fixture_versus
import bridge.feature.matches.impl.generated.resources.matchday_arena
import bridge.feature.matches.impl.generated.resources.matchday_capacity
import bridge.feature.matches.impl.generated.resources.matchday_days
import bridge.feature.matches.impl.generated.resources.matchday_fixture_failed
import bridge.feature.matches.impl.generated.resources.matchday_founded
import bridge.feature.matches.impl.generated.resources.matchday_hours
import bridge.feature.matches.impl.generated.resources.matchday_kickoff_local
import bridge.feature.matches.impl.generated.resources.matchday_kickoff_now
import bridge.feature.matches.impl.generated.resources.matchday_loading_fixture
import bridge.feature.matches.impl.generated.resources.matchday_minutes
import bridge.feature.matches.impl.generated.resources.matchday_next_match
import bridge.feature.matches.impl.generated.resources.matchday_no_fixture
import bridge.feature.matches.impl.generated.resources.matchday_recent
import bridge.feature.matches.impl.generated.resources.matchday_seconds
import bridge.feature.matches.impl.generated.resources.matchday_stadium
import com.begoml.bridge.core.data.model.Club
import com.begoml.bridge.core.data.model.Match
import com.begoml.bridge.core.data.repository.ClubRepository
import com.begoml.bridge.core.data.repository.MatchRepository
import com.begoml.bridge.feature.club.api.ClubRoute
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString

/** Every fixed word on the matchday screen, resolved once away from the composition. */
data class MatchdayLabels(
    val nextMatch: String,
    val fixtureFailed: String,
    val noFixture: String,
    val loadingFixture: String,
    val kickoffLocal: String,
    val kickoffNow: String,
    val versus: String,
    val days: String,
    val hours: String,
    val minutes: String,
    val seconds: String,
    val recent: String,
    val stadium: String,
    val arena: String,
    val capacity: String,
    val founded: String,
)

/** A result row with its text already built, so the list draws strings rather than formats them. */
data class RecentMatchUi(
    val teams: String,
    val competition: String,
    val score: String?,
    val awayBadgeUrl: String?,
    val awayCode: String,
)

data class MatchdayUiState(
    val club: Club? = null,
    val nextMatch: Match? = null,
    val recent: RecentMatchUi? = null,
    val labels: MatchdayLabels? = null,
    val nextMatchLoaded: Boolean = false,
    val nextMatchFailed: Boolean = false,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
    val nowMillis: Long = 0L,
)

internal class MatchdayViewModel(
    clubRepository: ClubRepository,
    matchRepository: MatchRepository,
    nowMillis: () -> Long,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val feature = MatchdayFeature(
        scope = viewModelScope,
        clubRepository = clubRepository,
        matchRepository = matchRepository,
        nowMillis = nowMillis,
    )

    private val labels = MutableStateFlow<MatchdayLabels?>(null)

    val state: StateFlow<MatchdayUiState> =
        combine(feature.stateFlow, labels) { content, resolved ->
            MatchdayUiState(
                club = content.club,
                nextMatch = content.nextMatch,
                recent = content.lastResult?.let { withContext(ioDispatcher) { it.toUi() } },
                labels = resolved,
                nextMatchLoaded = content.nextMatchLoaded,
                nextMatchFailed = content.nextMatchFailed,
                isLoading = content.isLoading || resolved == null,
                error = content.error,
                nowMillis = content.nowMillis,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, MatchdayUiState())

    init {
        viewModelScope.launch { labels.value = withContext(ioDispatcher) { readLabels() } }
    }

    fun retry() {
        feature.dispatchAction(MatchdayAction.Retry)
    }

    fun onStadiumClick() {
        router.navigateTo(ClubRoute)
    }

    private suspend fun Match.toUi() = RecentMatchUi(
        teams = getString(Res.string.fixture_teams, home.name, away.name),
        competition = competition,
        score = score?.let { getString(Res.string.fixture_score, it.home, it.away) },
        awayBadgeUrl = away.badgeUrl,
        awayCode = away.code,
    )

    private suspend fun readLabels() = MatchdayLabels(
        nextMatch = getString(Res.string.matchday_next_match),
        fixtureFailed = getString(Res.string.matchday_fixture_failed),
        noFixture = getString(Res.string.matchday_no_fixture),
        loadingFixture = getString(Res.string.matchday_loading_fixture),
        kickoffLocal = getString(Res.string.matchday_kickoff_local),
        kickoffNow = getString(Res.string.matchday_kickoff_now),
        versus = getString(Res.string.fixture_versus),
        days = getString(Res.string.matchday_days),
        hours = getString(Res.string.matchday_hours),
        minutes = getString(Res.string.matchday_minutes),
        seconds = getString(Res.string.matchday_seconds),
        recent = getString(Res.string.matchday_recent),
        stadium = getString(Res.string.matchday_stadium),
        arena = getString(Res.string.matchday_arena),
        capacity = getString(Res.string.matchday_capacity),
        founded = getString(Res.string.matchday_founded),
    )
}
