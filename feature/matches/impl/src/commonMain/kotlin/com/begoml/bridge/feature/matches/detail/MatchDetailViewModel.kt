package com.begoml.bridge.feature.matches.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bridge.feature.matches.impl.generated.resources.Res
import bridge.feature.matches.impl.generated.resources.fixture_score
import bridge.feature.matches.impl.generated.resources.fixture_versus
import bridge.feature.matches.impl.generated.resources.match_away
import bridge.feature.matches.impl.generated.resources.match_back
import bridge.feature.matches.impl.generated.resources.match_draw
import bridge.feature.matches.impl.generated.resources.match_home
import bridge.feature.matches.impl.generated.resources.match_loss
import bridge.feature.matches.impl.generated.resources.match_win
import bridge.feature.matches.impl.generated.resources.season_round
import bridge.feature.matches.impl.generated.resources.match_kickoff
import bridge.feature.matches.impl.generated.resources.match_not_found
import bridge.feature.matches.impl.generated.resources.match_title
import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.core.domain.model.SeasonMatch
import com.begoml.bridge.core.domain.TeamNames
import com.begoml.bridge.core.domain.model.FollowedClub
import com.begoml.bridge.core.domain.repository.MatchRepository
import com.begoml.bridge.feature.matches.formatKickoff
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateUp
import com.begoml.bridge.foundation.tessera.UiStateDelegate
import com.begoml.bridge.foundation.tessera.UiStateDelegateImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString

data class MatchDetailLabels(
    val title: String,
    val back: String,
    val notFound: String,
    val kickoff: String,
    val homeLabel: String,
    val awayLabel: String,
    val win: String,
    val draw: String,
    val loss: String,
)

/** How the match ended for the club this build follows. Absent when it is not our match. */
enum class MatchOutcome { Win, Draw, Loss }

/** Which side of the fixture our club is on. Absent when neither side is ours. */
enum class MatchSide { Home, Away }

data class MatchDetailUi(
    val homeName: String,
    val homeCode: String,
    val awayName: String,
    val awayCode: String,
    val scoreline: String,
    val kickoff: String,
    val round: String,
    val side: MatchSide?,
    val outcome: MatchOutcome?,
)

data class MatchDetailUiState(
    val match: MatchDetailUi? = null,
    /** True until both the fixture and the labels have answered; absent is not the same as loading. */
    val isLoading: Boolean = true,
)

internal class MatchDetailViewModel(
    matchId: String,
    scope: CoroutineScope,
    private val matchRepository: MatchRepository,
    private val club: FollowedClub,
    val labels: MatchDetailLabels,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(scope),
    UiStateDelegate<MatchDetailUiState> by UiStateDelegateImpl(MatchDetailUiState()) {

    init {
        viewModelScope.launch {
            matchRepository.match(matchId).collect { loadable ->
                val match = (loadable as? Loadable.Content)?.value
                val ui = match?.let { withContext(ioDispatcher) { it.toUi() } }
                updateUiState {
                    MatchDetailUiState(match = ui, isLoading = loadable is Loadable.Loading)
                }
            }
        }
    }


    fun onBack() {
        router.navigateUp()
    }

    private fun SeasonMatch.side(): MatchSide? = when {
        TeamNames.matches(home.name, club.name) -> MatchSide.Home
        TeamNames.matches(away.name, club.name) -> MatchSide.Away
        else -> null
    }

    /**
     * The result read from our club's point of view.
     *
     * Null for a fixture that has not been played and for a match we are not in — both are absent
     * results, and neither is a draw.
     */
    private fun SeasonMatch.outcome(side: MatchSide?): MatchOutcome? {
        val result = score ?: return null
        val ours = when (side ?: return null) {
            MatchSide.Home -> result.home to result.away
            MatchSide.Away -> result.away to result.home
        }
        return when {
            ours.first > ours.second -> MatchOutcome.Win
            ours.first < ours.second -> MatchOutcome.Loss
            else -> MatchOutcome.Draw
        }
    }

    private suspend fun SeasonMatch.toUi(): MatchDetailUi {
        val side = side()
        return MatchDetailUi(
        homeName = home.name,
        homeCode = home.code,
        awayName = away.name,
        awayCode = away.code,
        scoreline = score
            ?.let { getString(Res.string.fixture_score, it.home, it.away) }
            ?: getString(Res.string.fixture_versus),
        kickoff = kickoff.formatKickoff(),
        round = getString(Res.string.season_round, round),
        side = side,
        outcome = outcome(side),
        )
    }

}

/** Read once for the whole run: the words do not change while the app is open. */
suspend fun loadMatchDetailLabels() = MatchDetailLabels(
    title = getString(Res.string.match_title),
    back = getString(Res.string.match_back),
    notFound = getString(Res.string.match_not_found),
    kickoff = getString(Res.string.match_kickoff),
    homeLabel = getString(Res.string.match_home),
    awayLabel = getString(Res.string.match_away),
    win = getString(Res.string.match_win),
    draw = getString(Res.string.match_draw),
    loss = getString(Res.string.match_loss),
)
