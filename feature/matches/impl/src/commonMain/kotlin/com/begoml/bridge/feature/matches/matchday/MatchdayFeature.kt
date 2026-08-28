package com.begoml.bridge.feature.matches.matchday

import com.begoml.bridge.core.domain.model.Club
import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.core.domain.model.Match
import com.begoml.bridge.core.domain.model.Player
import com.begoml.bridge.core.domain.model.FollowedClub
import com.begoml.bridge.core.domain.repository.ClubRepository
import com.begoml.bridge.core.domain.repository.MatchRepository
import com.begoml.bridge.core.domain.repository.SquadRepository
import com.begoml.bridge.core.features.following.FollowingFeature
import com.begoml.bridge.foundation.tessera.Feature
import com.begoml.bridge.foundation.tessera.awaitActionsIn
import com.begoml.bridge.foundation.tessera.composeState
import com.begoml.bridge.foundation.tessera.feature
import com.begoml.bridge.foundation.tessera.withInitial
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine

data class MatchdayState(
    val club: Club? = null,
    val nextMatch: Match? = null,
    val lastResult: Match? = null,
    /** The followed players who are in the current squad, in squad order. */
    val followedPlayers: ImmutableList<Player> = persistentListOf(),
    /**
     * Whether the fixture source has answered yet.
     *
     * Separate from [isLoading] because the club arrives from disk instantly while the fixture is
     * still on the network: without this the screen would announce "no fixture" for a second every
     * cold start, which is a different statement from "still asking".
     */
    val nextMatchLoaded: Boolean = false,
    /** A fixture that failed to load is not the same statement as a club with no fixture. */
    val nextMatchFailed: Boolean = false,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)

sealed interface MatchdayAction {
    data object Retry : MatchdayAction
}

/**
 * The matchday screen's state, folded from three independent sources.
 *
 * The countdown is not driven here. A clock owned by the feature runs for as long as the feature
 * does, which is the whole session; the ViewModel supplies the current time instead, so it stops
 * when nobody is looking at the screen.
 */
class MatchdayFeature(
    private val scope: CoroutineScope,
    private val club: FollowedClub,
    private val clubRepository: ClubRepository,
    private val matchRepository: MatchRepository,
    private val squadRepository: SquadRepository,
    private val following: FollowingFeature,
) : Feature<MatchdayState, MatchdayAction> by feature(MatchdayState(), scope) {

    init {
        observeSources()
        observeFollowed()
        awaitActionsIn(scope) { action ->
            when (action) {
                // Retry fetches; it does not resubscribe. Re-collecting was how a fetch used to
                // be triggered, which meant every retry left another collector writing into one
                // state and re-seeded the screen with Loading after it had already drawn.
                MatchdayAction.Retry -> {
                    clubRepository.refresh(club.id)
                    matchRepository.refreshFixtures(club.id)
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
                following.stateFlow,
                squadRepository.squad(club.id),
            ) { followed, squad ->
                (squad as? Loadable.Content)?.value.orEmpty()
                    .filter { player -> followed.contains(player.id) }
                    .toImmutableList()
            }.withInitial(scope, persistentListOf()),
        ) { state, players -> state.copy(followedPlayers = players) }
    }

    private fun observeSources() {
        composeState(
            scope = scope,
            source1 = clubRepository.club(club.id).withInitial(scope, Loadable.Loading),
            source2 = matchRepository.nextMatch(club.id).withInitial(scope, Loadable.Loading),
            source3 = matchRepository.lastResult(club.id).withInitial(scope, Loadable.Loading),
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
