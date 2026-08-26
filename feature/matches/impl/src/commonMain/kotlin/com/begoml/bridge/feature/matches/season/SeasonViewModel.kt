package com.begoml.bridge.feature.matches.season

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bridge.feature.matches.impl.generated.resources.Res
import bridge.feature.matches.impl.generated.resources.fixture_score
import bridge.feature.matches.impl.generated.resources.fixture_teams
import bridge.feature.matches.impl.generated.resources.season_round
import com.begoml.bridge.core.data.model.SeasonRound
import com.begoml.bridge.core.data.repository.MatchRepository
import com.begoml.bridge.feature.matches.api.MatchDetailRoute
import com.begoml.bridge.feature.matches.formatDay
import com.begoml.bridge.feature.matches.formatTime
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateTo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString

/**
 * One fixture, with every string it draws already built.
 *
 * A season is 380 of these and the whole calendar arrives in a single response, so the rows are
 * formatted once when the season loads instead of on each pass through a list item.
 */
data class FixtureRowUi(
    val id: String,
    val homeCode: String,
    val teams: String,
    val day: String,
    val trailing: String,
    val hasScore: Boolean,
    val highlighted: Boolean,
)

data class SeasonRoundUi(
    val number: Int,
    val title: String,
    val matches: ImmutableList<FixtureRowUi>,
)

data class SeasonUiState(
    val rounds: ImmutableList<SeasonRoundUi> = persistentListOf(),
    val initialRoundIndex: Int = 0,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)

/** How long a state flow outlives its last collector, so a rotation does not refetch. */
private const val SubscriptionTimeoutMillis = 5_000L

internal class SeasonViewModel(
    private val matchRepository: MatchRepository,
    nowMillis: () -> Long,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val feature = SeasonFeature(
        scope = viewModelScope,
        matchRepository = matchRepository,
        nowMillis = nowMillis,
    )

    val state: StateFlow<SeasonUiState> = feature.stateFlow
        .map { content ->
            SeasonUiState(
                rounds = withContext(ioDispatcher) { content.rounds.toUi() },
                initialRoundIndex = content.initialRoundIndex,
                isLoading = content.isLoading,
                error = content.error,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SubscriptionTimeoutMillis), SeasonUiState())

    fun retry() {
        feature.dispatchAction(SeasonAction.Retry)
    }

    fun onMatchClick(matchId: String) {
        router.navigateTo(MatchDetailRoute(matchId))
    }

    private suspend fun List<SeasonRound>.toUi(): ImmutableList<SeasonRoundUi> = map { round ->
        SeasonRoundUi(
            number = round.number,
            title = getString(Res.string.season_round, round.number),
            matches = round.matches.map { match ->
                val score = match.score
                FixtureRowUi(
                    id = match.id,
                    homeCode = match.home.code,
                    teams = getString(Res.string.fixture_teams, match.home.name, match.away.name),
                    day = match.kickoff.formatDay(),
                    trailing = score
                        ?.let { getString(Res.string.fixture_score, it.home, it.away) }
                        ?: match.kickoff.formatTime(),
                    hasScore = score != null,
                    highlighted = matchRepository.isOurs(match.home.name, match.away.name),
                )
            }.toImmutableList(),
        )
    }.toImmutableList()
}
