package com.begoml.bridge.feature.matches.matchday

import com.begoml.bridge.feature.matches.fixture_score
import com.begoml.bridge.feature.matches.fixture_teams
import com.begoml.bridge.feature.matches.fixture_versus
import com.begoml.bridge.feature.matches.matchday_arena
import com.begoml.bridge.feature.matches.matchday_capacity
import com.begoml.bridge.feature.matches.matchday_days
import com.begoml.bridge.feature.matches.matchday_fixture_failed
import com.begoml.bridge.feature.matches.matchday_following
import com.begoml.bridge.feature.matches.matchday_founded
import com.begoml.bridge.feature.matches.matchday_hours
import com.begoml.bridge.feature.matches.matchday_kickoff_local
import com.begoml.bridge.feature.matches.matchday_kickoff_now
import com.begoml.bridge.feature.matches.matchday_loading_fixture
import com.begoml.bridge.feature.matches.matchday_minutes
import com.begoml.bridge.feature.matches.matchday_next_match
import com.begoml.bridge.feature.matches.matchday_no_fixture
import com.begoml.bridge.feature.matches.matchday_recent
import com.begoml.bridge.feature.matches.matchday_seconds
import com.begoml.bridge.feature.matches.matchday_stadium
import com.begoml.bridge.feature.matches.MatchesStrings
import com.begoml.bridge.foundation.logger.Logger
import com.begoml.bridge.foundation.coroutines.safeLaunch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
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

private const val Tag = "Matchday"

/** Every fixed word on the matchday screen, resolved once away from the composition. */
/**
 * Every fixed word on this screen — as descriptions, not as words.
 *
 * A [StringDesc] names which string is meant; the platform that draws it decides what that reads
 * like, because only it knows the device's locale. Nothing is resolved here, so nothing has to be
 * awaited before the first frame.
 */
data class MatchdayLabels(
    val nextMatch: StringDesc = MatchesStrings.strings.matchday_next_match.desc(),
    val fixtureFailed: StringDesc = MatchesStrings.strings.matchday_fixture_failed.desc(),
    val noFixture: StringDesc = MatchesStrings.strings.matchday_no_fixture.desc(),
    val loadingFixture: StringDesc = MatchesStrings.strings.matchday_loading_fixture.desc(),
    val kickoffLocal: StringDesc = MatchesStrings.strings.matchday_kickoff_local.desc(),
    val kickoffNow: StringDesc = MatchesStrings.strings.matchday_kickoff_now.desc(),
    val versus: StringDesc = MatchesStrings.strings.fixture_versus.desc(),
    val days: StringDesc = MatchesStrings.strings.matchday_days.desc(),
    val hours: StringDesc = MatchesStrings.strings.matchday_hours.desc(),
    val minutes: StringDesc = MatchesStrings.strings.matchday_minutes.desc(),
    val seconds: StringDesc = MatchesStrings.strings.matchday_seconds.desc(),
    val recent: StringDesc = MatchesStrings.strings.matchday_recent.desc(),
    val following: StringDesc = MatchesStrings.strings.matchday_following.desc(),
    val stadium: StringDesc = MatchesStrings.strings.matchday_stadium.desc(),
    val arena: StringDesc = MatchesStrings.strings.matchday_arena.desc(),
    val capacity: StringDesc = MatchesStrings.strings.matchday_capacity.desc(),
    val founded: StringDesc = MatchesStrings.strings.matchday_founded.desc(),
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
    val teams: StringDesc,
    val competition: String,
    val score: StringDesc?,
    val awayBadgeUrl: String?,
    val awayCode: String,
)

data class MatchdayUiState(
    val labels: MatchdayLabels = MatchdayLabels(),
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

    private fun Match.toRecentUi() = RecentMatchUi(
        teams = StringDesc.ResourceFormatted(MatchesStrings.strings.fixture_teams, home.name, away.name),
        competition = competition,
        score = score?.let { StringDesc.ResourceFormatted(MatchesStrings.strings.fixture_score, it.home, it.away) },
        awayBadgeUrl = away.badgeUrl,
        awayCode = away.code,
    )

}
