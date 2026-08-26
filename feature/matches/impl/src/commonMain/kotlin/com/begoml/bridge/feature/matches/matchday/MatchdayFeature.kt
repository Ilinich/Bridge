package com.begoml.bridge.feature.matches.matchday

import com.begoml.bridge.core.data.model.Club
import com.begoml.bridge.core.data.model.Loadable
import com.begoml.bridge.core.data.model.Match
import com.begoml.bridge.core.data.repository.ClubRepository
import com.begoml.bridge.core.data.repository.MatchRepository
import com.begoml.bridge.foundation.tessera.SimpleFeature
import com.begoml.bridge.foundation.tessera.awaitActionsIn
import com.begoml.bridge.foundation.tessera.composeState
import com.begoml.bridge.foundation.tessera.feature
import com.begoml.bridge.foundation.tessera.withInitial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MatchdayState(
    val club: Club? = null,
    val nextMatch: Match? = null,
    val lastResult: Match? = null,
    /**
     * Whether the fixture source has answered yet.
     *
     * Separate from [isLoading] because the club now arrives from disk instantly while the fixture
     * is still on the network: without this the screen would announce "no fixture" for a second
     * every cold start, which is a different statement from "still asking".
     */
    val nextMatchLoaded: Boolean = false,
    /** A fixture that failed to load is not the same statement as a club with no fixture. */
    val nextMatchFailed: Boolean = false,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
    val nowMillis: Long = 0L,
)

sealed interface MatchdayAction {
    data object Retry : MatchdayAction
}

sealed interface MatchdayEvent

private const val TickMillis = 1000L

/**
 * The matchday screen's state, folded from three independent sources.
 *
 * The countdown ticks here rather than in the composable: the state carries a kick-off instant and
 * the current time, and the screen only renders the difference. A timer that lived in composition
 * would restart on every recomposition and stop being a clock.
 */
class MatchdayFeature(
    private val scope: CoroutineScope,
    private val clubRepository: ClubRepository,
    private val matchRepository: MatchRepository,
    private val nowMillis: () -> Long,
) : SimpleFeature<MatchdayState, MatchdayAction, MatchdayEvent> by feature(MatchdayState(), scope) {

    init {
        observeSources()
        startClock()
        awaitActionsIn(scope) { action ->
            when (action) {
                MatchdayAction.Retry -> {
                    clubRepository.refresh()
                    observeSources()
                }
            }
        }
    }

    private fun observeSources() {
        composeState(
            scope = scope,
            source1 = clubRepository.club().withInitial(scope, Loadable.Loading),
            source2 = matchRepository.nextMatch().withInitial(scope, Loadable.Loading),
            source3 = matchRepository.lastResult().withInitial(scope, Loadable.Loading),
        ) { state, club, next, last ->
            state.copy(
                club = (club as? Loadable.Content)?.value ?: state.club,
                nextMatch = (next as? Loadable.Content)?.value ?: state.nextMatch,
                nextMatchLoaded = state.nextMatchLoaded || next !is Loadable.Loading,
                nextMatchFailed = next is Loadable.Failed,
                lastResult = (last as? Loadable.Content)?.value ?: state.lastResult,
                isLoading = listOf(club, next, last).any { it is Loadable.Loading },
                error = listOf(club, next, last).filterIsInstance<Loadable.Failed>()
                    .firstOrNull()
                    ?.error,
            )
        }
    }

    private fun startClock() {
        scope.launch {
            while (true) {
                updateStateAsync { state -> state.copy(nowMillis = nowMillis()) }
                delay(TickMillis)
            }
        }
    }
}
