package com.begoml.bridge.feature.matches.detail

import com.begoml.bridge.feature.matches.fixture_score
import com.begoml.bridge.feature.matches.fixture_versus
import com.begoml.bridge.feature.matches.match_away
import com.begoml.bridge.feature.matches.match_back
import com.begoml.bridge.feature.matches.match_draw
import com.begoml.bridge.feature.matches.match_home
import com.begoml.bridge.feature.matches.match_kickoff
import com.begoml.bridge.feature.matches.match_loss
import com.begoml.bridge.feature.matches.match_not_found
import com.begoml.bridge.feature.matches.match_title
import com.begoml.bridge.feature.matches.match_win
import com.begoml.bridge.feature.matches.season_round
import com.begoml.bridge.feature.matches.MatchesStrings
import com.begoml.bridge.foundation.logger.Logger
import com.begoml.bridge.foundation.coroutines.safeLaunch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.core.domain.model.SeasonMatch
import com.begoml.bridge.core.domain.TeamNames
import com.begoml.bridge.core.domain.model.FollowedClub
import com.begoml.bridge.core.domain.repository.MatchRepository
import com.begoml.bridge.feature.matches.formatKickoff
import com.begoml.bridge.navigation.router.AppRouter
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import com.begoml.bridge.navigation.router.navigateUp
import com.begoml.bridge.foundation.tessera.UiStateDelegate
import com.begoml.bridge.foundation.tessera.UiStateDelegateImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

private const val Tag = "MatchDetail"

/**
 * Every fixed word on this screen — as descriptions, not as words.
 *
 * A [StringDesc] names which string is meant; the platform that draws it decides what that reads
 * like, because only it knows the device's locale. Nothing is resolved here, so nothing has to be
 * awaited before the first frame.
 */
data class MatchDetailLabels(
    val title: StringDesc = MatchesStrings.strings.match_title.desc(),
    val back: StringDesc = MatchesStrings.strings.match_back.desc(),
    val notFound: StringDesc = MatchesStrings.strings.match_not_found.desc(),
    val kickoff: StringDesc = MatchesStrings.strings.match_kickoff.desc(),
    val homeLabel: StringDesc = MatchesStrings.strings.match_home.desc(),
    val awayLabel: StringDesc = MatchesStrings.strings.match_away.desc(),
    val win: StringDesc = MatchesStrings.strings.match_win.desc(),
    val draw: StringDesc = MatchesStrings.strings.match_draw.desc(),
    val loss: StringDesc = MatchesStrings.strings.match_loss.desc(),
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
    val scoreline: StringDesc,
    val kickoff: String,
    val round: StringDesc,
    val side: MatchSide?,
    val outcome: MatchOutcome?,
)

data class MatchDetailUiState(
    val labels: MatchDetailLabels = MatchDetailLabels(),
    val match: MatchDetailUi? = null,
    /** True until the fixture has answered; absent is not the same as loading. */
    val isLoading: Boolean = true,
)

/**
 * Public because a platform UI is the thing that reads it: on iOS the screen is Swift,
 * and a Swift view cannot see an internal Kotlin class.
 */
class MatchDetailViewModel(
    matchId: String,
    scope: CoroutineScope,
    private val matchRepository: MatchRepository,
    private val club: FollowedClub,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger,
) : ViewModel(scope),
    UiStateDelegate<MatchDetailUiState> by UiStateDelegateImpl(MatchDetailUiState()) {

    init {
        viewModelScope.safeLaunch(dispatcher = ioDispatcher, logger = logger, tag = Tag) {
            matchRepository.match(matchId).collect { loadable ->
                val match = (loadable as? Loadable.Content)?.value
                val ui = match?.let { it.toUi() }
                updateUiState {
                    MatchDetailUiState(
                        match = ui,
                        isLoading = loadable is Loadable.Loading,
                    )
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

    private fun SeasonMatch.toUi(): MatchDetailUi {
        val side = side()
        return MatchDetailUi(
        homeName = home.name,
        homeCode = home.code,
        awayName = away.name,
        awayCode = away.code,
        scoreline = score
            ?.let { StringDesc.ResourceFormatted(MatchesStrings.strings.fixture_score, it.home, it.away) }
            ?: MatchesStrings.strings.fixture_versus.desc(),
        kickoff = kickoff.formatKickoff(),
        round = StringDesc.ResourceFormatted(MatchesStrings.strings.season_round, round),
        side = side,
        outcome = outcome(side),
        )
    }

}
