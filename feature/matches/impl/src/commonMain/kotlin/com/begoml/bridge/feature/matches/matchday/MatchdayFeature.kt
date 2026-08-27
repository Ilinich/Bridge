package com.begoml.bridge.feature.matches.matchday

import com.begoml.bridge.core.data.model.Club
import com.begoml.bridge.core.data.model.Loadable
import com.begoml.bridge.core.data.model.Match
import com.begoml.bridge.core.data.model.Player
import com.begoml.bridge.core.data.repository.ClubRepository
import com.begoml.bridge.core.data.repository.MatchRepository
import com.begoml.bridge.core.data.repository.SquadRepository
import com.begoml.bridge.core.favourites.FavouritesFeature
import com.begoml.bridge.foundation.tessera.SimpleFeature
import com.begoml.bridge.foundation.tessera.awaitActionsIn
import com.begoml.bridge.foundation.tessera.composeState
import com.begoml.bridge.foundation.tessera.feature
import com.begoml.bridge.foundation.tessera.withInitial
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.Job

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
    /** The followed players who are in the current squad, in squad order. */
    val followedPlayers: ImmutableList<Player> = persistentListOf(),
    val nextMatchLoaded: Boolean = false,
    /** A fixture that failed to load is not the same statement as a club with no fixture. */
    val nextMatchFailed: Boolean = false,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)

sealed interface MatchdayAction {
    data object Retry : MatchdayAction
}

sealed interface MatchdayEvent

/**
 * The matchday screen's state, folded from three independent sources.
 *
 * The countdown is not driven here. A clock owned by the feature runs for as long as the feature
 * does, which is the whole session; the ViewModel supplies the current time instead, so it stops
 * when nobody is looking at the screen.
 */
class MatchdayFeature(
    private val scope: CoroutineScope,
    private val clubRepository: ClubRepository,
    private val matchRepository: MatchRepository,
    private val squadRepository: SquadRepository,
    private val favourites: FavouritesFeature,
) : SimpleFeature<MatchdayState, MatchdayAction, MatchdayEvent> by feature(MatchdayState(), scope) {

    /**
     * The subscription Retry replaces.
     *
     * Without it every Retry left the previous collector running: several chains then wrote to one
     * state, and a stale one could put a screen that had already rendered back into loading.
     */
    private var sourcesJob: Job? = null

    init {
        observeSources()
        observeFollowed()
        awaitActionsIn(scope) { action ->
            when (action) {
                MatchdayAction.Retry -> {
                    clubRepository.refresh()
                    observeSources()
                }
            }
        }
    }

    /**
     * Who the person follows, named.
     *
     * The set is written on another screen in another feature module and outlives both, so it
     * arrives as a feature rather than as this screen's state. Only the squad turns ids into
     * players, so an id no longer in the squad simply drops out.
     */
    private fun observeFollowed() {
        composeState(
            scope = scope,
            source = combine(
                favourites.stateFlow,
                squadRepository.squad(),
            ) { followed, squad ->
                (squad as? Loadable.Content)?.value.orEmpty()
                    .filter { player -> followed.contains(player.id) }
                    .toImmutableList()
            }.withInitial(scope, persistentListOf()),
        ) { state, players -> state.copy(followedPlayers = players) }
    }

    private fun observeSources() {
        sourcesJob?.cancel()
        sourcesJob = composeState(
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

}
