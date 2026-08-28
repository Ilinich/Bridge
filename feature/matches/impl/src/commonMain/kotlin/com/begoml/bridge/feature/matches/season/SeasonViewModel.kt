package com.begoml.bridge.feature.matches.season

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bridge.feature.matches.impl.generated.resources.Res
import bridge.feature.matches.impl.generated.resources.fixture_score
import bridge.feature.matches.impl.generated.resources.fixture_teams
import bridge.feature.matches.impl.generated.resources.season_round
import com.begoml.bridge.core.domain.model.SeasonRound
import com.begoml.bridge.core.connectivity.Connectivity
import com.begoml.bridge.core.connectivity.NetworkStatus
import com.begoml.bridge.core.domain.TeamNames
import com.begoml.bridge.core.domain.model.FollowedClub
import com.begoml.bridge.core.analytics.Analytics
import com.begoml.bridge.feature.matches.analytics.MatchOpened
import com.begoml.bridge.feature.matches.api.MatchDetailRoute
import com.begoml.bridge.feature.matches.formatDay
import com.begoml.bridge.feature.matches.formatTime
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateTo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import com.begoml.bridge.foundation.tessera.UiStateDelegate
import com.begoml.bridge.foundation.tessera.UiStateDelegateImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
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
    val isOffline: Boolean = false,
)

/**
 * The season screen.
 *
 * The feature arrives built rather than being constructed here, and both it and this share one
 * scope created outside them: a state holder whose lifetime is decided by whoever happens to own
 * it is the thing that leaks. The scope is handed to [ViewModel], which cancels it when the screen
 * is gone — so the feature ends with the ViewModel without either of them saying so.
 */
internal class SeasonViewModel(
    scope: CoroutineScope,
    private val feature: SeasonFeature,
    private val connectivity: Connectivity,
    private val club: FollowedClub,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
    private val analytics: Analytics,
) : ViewModel(scope),
    UiStateDelegate<SeasonUiState> by UiStateDelegateImpl(SeasonUiState()) {

    init {
        viewModelScope.launch {
            combine(feature.stateFlow, connectivity.status) { content, network ->
                SeasonUiState(
                    rounds = withContext(ioDispatcher) { content.rounds.toUi() },
                    initialRoundIndex = content.initialRoundIndex,
                    isLoading = content.isLoading,
                    error = content.error,
                    isOffline = network == NetworkStatus.Offline,
                )
            }.collect { built -> updateUiState { built } }
        }
    }


    fun retry() {
        feature.dispatchAction(SeasonAction.Retry)
    }

    fun onMatchClick(matchId: String) {
        analytics.track(MatchOpened(matchId))
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
                    highlighted = TeamNames.matches(match.home.name, club.name) ||
                        TeamNames.matches(match.away.name, club.name),
                )
            }.toImmutableList(),
        )
    }.toImmutableList()
}
