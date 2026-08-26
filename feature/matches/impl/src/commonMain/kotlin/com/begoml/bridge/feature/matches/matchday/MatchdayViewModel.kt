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
import com.begoml.bridge.feature.matches.formatKickoff
import com.begoml.bridge.uikit.groupedThousands
import com.begoml.bridge.core.data.model.Match
import com.begoml.bridge.core.data.repository.ClubRepository
import com.begoml.bridge.core.data.repository.MatchRepository
import com.begoml.bridge.feature.club.api.ClubRoute
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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

/** The fixture with its text already built, so the clock cannot make the screen reformat it. */
data class NextMatchUi(
    val competition: String,
    val venue: String?,
    val kickoffText: String,
    val kickoffMillis: Long,
    val homeName: String,
    val homeCode: String,
    val homeBadgeUrl: String?,
    val awayName: String,
    val awayCode: String,
    val awayBadgeUrl: String?,
)

/** The ground facts, formatted once rather than on every tick. */
data class StadiumUi(
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
    val backdropUrl: String? = null,
    val stadium: StadiumUi? = null,
    val hasClub: Boolean = false,
    val nextMatch: NextMatchUi? = null,
    val recent: RecentMatchUi? = null,
    val labels: MatchdayLabels? = null,
    val nextMatchLoaded: Boolean = false,
    val nextMatchFailed: Boolean = false,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
    val nowMillis: Long = 0L,
)

/** How long a state flow outlives its last collector, so a rotation does not refetch. */
private const val SubscriptionTimeoutMillis = 5_000L
private const val TickMillis = 1_000L

internal class MatchdayViewModel(
    clubRepository: ClubRepository,
    matchRepository: MatchRepository,
    private val nowMillis: () -> Long,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val feature = MatchdayFeature(
        scope = viewModelScope,
        clubRepository = clubRepository,
        matchRepository = matchRepository,
    )

    private val labels = MutableStateFlow<MatchdayLabels?>(null)

    /**
     * The countdown's clock.
     *
     * A flow rather than a loop on viewModelScope: combined into [state], it only runs while
     * something collects, so a screen the user has left stops ticking.
     */
    private val ticker: Flow<Long> = flow {
        while (true) {
            emit(nowMillis())
            delay(TickMillis)
        }
    }

    /** Built only when the result itself changes; the clock must not re-format it every second. */
    private val recent: Flow<RecentMatchUi?> = feature.stateFlow
        .map { it.lastResult }
        .distinctUntilChanged()
        .map { match -> match?.let { withContext(ioDispatcher) { it.toRecentUi() } } }

    private val nextMatch: Flow<NextMatchUi?> = feature.stateFlow
        .map { it.nextMatch }
        .distinctUntilChanged()
        .map { match -> match?.toUi() }

    private val stadium: Flow<StadiumUi?> = feature.stateFlow
        .map { it.club }
        .distinctUntilChanged()
        .map { club -> club?.toStadiumUi() }

    val state: StateFlow<MatchdayUiState> =
        combine(
            feature.stateFlow,
            labels,
            recent,
            ticker,
            combine(nextMatch, stadium) { next, ground -> next to ground },
        ) { content, resolved, recentUi, now, fixtureAndGround ->
            val (nextMatchUi, stadiumUi) = fixtureAndGround
            MatchdayUiState(
                backdropUrl = content.club?.media?.fanartUrls?.firstOrNull(),
                stadium = stadiumUi,
                hasClub = content.club != null,
                nextMatch = nextMatchUi,
                recent = recentUi,
                labels = resolved,
                nextMatchLoaded = content.nextMatchLoaded,
                nextMatchFailed = content.nextMatchFailed,
                isLoading = content.isLoading || resolved == null,
                error = content.error,
                nowMillis = now,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SubscriptionTimeoutMillis),
            MatchdayUiState(),
        )

    init {
        viewModelScope.launch { labels.value = withContext(ioDispatcher) { readLabels() } }
    }

    fun retry() {
        feature.dispatchAction(MatchdayAction.Retry)
    }

    fun onStadiumClick() {
        router.navigateTo(ClubRoute)
    }

    private fun Match.toUi() = NextMatchUi(
        competition = competition,
        venue = venue,
        kickoffText = kickoff.formatKickoff(),
        kickoffMillis = kickoff.toEpochMilliseconds(),
        homeName = home.name,
        homeCode = home.code,
        homeBadgeUrl = home.badgeUrl,
        awayName = away.name,
        awayCode = away.code,
        awayBadgeUrl = away.badgeUrl,
    )

    private fun Club.toStadiumUi() = StadiumUi(
        arena = stadium.orEmpty(),
        capacity = stadiumCapacity?.groupedThousands().orEmpty(),
        founded = foundedYear?.toString().orEmpty(),
    )

    private suspend fun Match.toRecentUi() = RecentMatchUi(
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
