package com.begoml.bridge.feature.matches.matchday

import com.begoml.bridge.foundation.logger.Logger
import com.begoml.bridge.foundation.coroutines.safeLaunch
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
import bridge.feature.matches.impl.generated.resources.matchday_following
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
import com.begoml.bridge.core.domain.model.Club
import kotlin.time.Clock
import com.begoml.bridge.feature.matches.formatKickoff
import com.begoml.bridge.uikit.groupedThousands
import com.begoml.bridge.core.domain.model.Match
import com.begoml.bridge.foundation.tessera.UiStateDelegate
import com.begoml.bridge.foundation.tessera.UiStateDelegateImpl
import com.begoml.bridge.core.connectivity.Connectivity
import com.begoml.bridge.core.connectivity.NetworkStatus
import com.begoml.bridge.feature.club.api.ClubRoute
import com.begoml.bridge.feature.player.api.PlayerDetailRoute
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateTo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import org.jetbrains.compose.resources.getString

private const val Tag = "Matchday"

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
    val following: String,
    val stadium: String,
    val arena: String,
    val capacity: String,
    val founded: String,
)

/** A followed player, carrying the id the screen needs to open them. */
data class FollowedPlayerUi(val id: String, val name: String)

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
    /** The followed players, in squad order; empty when nobody is followed. */
    val following: ImmutableList<FollowedPlayerUi> = persistentListOf(),
    val nextMatchLoaded: Boolean = false,
    val nextMatchFailed: Boolean = false,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
    val isOffline: Boolean = false,
)

private const val TickMillis = 1_000L

internal class MatchdayViewModel(
    scope: CoroutineScope,
    private val feature: MatchdayFeature,
    private val connectivity: Connectivity,
    private val clock: Clock,
    val labels: MatchdayLabels,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger,
) : ViewModel(scope),
    UiStateDelegate<MatchdayUiState> by UiStateDelegateImpl(MatchdayUiState()) {

    /**
     * The countdown's clock.
     *
     * Cold on purpose: the screen collects it through the lifecycle, so a second's tick costs
     * nothing while the user is elsewhere. It is deliberately not part of the ui state — a state
     * that rebuilt every second would recompose the whole screen for one line of text.
     */
    val ticker: Flow<Long> = flow {
        while (true) {
            emit(clock.now().toEpochMilliseconds())
            delay(TickMillis)
        }
    }

    /** Built only when the result itself changes; the clock must not re-format it every second. */
    private val recent: Flow<RecentMatchUi?> = feature.stateFlow
        .map { it.lastResult }
        .distinctUntilChanged()
        .map { match -> match?.let { it.toRecentUi() } }

    private val nextMatch: Flow<NextMatchUi?> = feature.stateFlow
        .map { it.nextMatch }
        .distinctUntilChanged()
        .map { match -> match?.toUi() }

    private val stadium: Flow<StadiumUi?> = feature.stateFlow
        .map { it.club }
        .distinctUntilChanged()
        .map { club -> club?.toStadiumUi() }

    init {
        viewModelScope.safeLaunch(dispatcher = ioDispatcher, logger = logger, tag = Tag) {
            combine(
                feature.stateFlow,
                recent,
                nextMatch,
                stadium,
                connectivity.status,
            ) { content, recentUi, nextMatchUi, stadiumUi, network ->
                MatchdayUiState(
                    backdropUrl = content.club?.media?.fanartUrls?.firstOrNull(),
                    stadium = stadiumUi,
                    hasClub = content.club != null,
                    nextMatch = nextMatchUi,
                    recent = recentUi,
                    following = content.followedPlayers
                        .map { player -> FollowedPlayerUi(id = player.id, name = player.name) }
                        .toImmutableList(),
                    nextMatchLoaded = content.nextMatchLoaded,
                    nextMatchFailed = content.nextMatchFailed,
                    isLoading = content.isLoading,
                    error = content.error,
                    isOffline = network == NetworkStatus.Offline,
                )
            }.collect { built -> updateUiState { built } }
        }
    }

    fun nowMillis(): Long = clock.now().toEpochMilliseconds()


    fun retry() {
        feature.dispatchAction(MatchdayAction.Retry)
    }

    fun onStadiumClick() {
        router.navigateTo(ClubRoute)
    }

    fun onFollowedPlayerClick(playerId: String) {
        router.navigateTo(PlayerDetailRoute(playerId))
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

}

/** Read once for the whole run: the words do not change while the app is open. */
suspend fun loadMatchdayLabels() = MatchdayLabels(
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
    following = getString(Res.string.matchday_following),
    stadium = getString(Res.string.matchday_stadium),
    arena = getString(Res.string.matchday_arena),
    capacity = getString(Res.string.matchday_capacity),
    founded = getString(Res.string.matchday_founded),
)
