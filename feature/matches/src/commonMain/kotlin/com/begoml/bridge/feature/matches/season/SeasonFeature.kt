package com.begoml.bridge.feature.matches.season

import com.begoml.bridge.core.data.model.Loadable
import com.begoml.bridge.core.data.model.Season
import com.begoml.bridge.core.data.model.SeasonRound
import com.begoml.bridge.core.data.repository.MatchRepository
import com.begoml.bridge.foundation.tessera.SimpleFeature
import com.begoml.bridge.foundation.tessera.awaitActionsIn
import com.begoml.bridge.foundation.tessera.composeState
import com.begoml.bridge.foundation.tessera.feature
import com.begoml.bridge.foundation.tessera.withInitial
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope

data class SeasonState(
    val rounds: ImmutableList<SeasonRound> = persistentListOf(),
    val initialRoundIndex: Int = 0,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)

sealed interface SeasonAction {
    data object Retry : SeasonAction
}

sealed interface SeasonEvent

/**
 * The season calendar.
 *
 * All 380 fixtures arrive in one response, so every round is in memory once the screen has loaded
 * and paging between them never touches the network. The starting round is the next one still to
 * be played rather than the first of the season.
 */
class SeasonFeature(
    private val scope: CoroutineScope,
    private val matchRepository: MatchRepository,
    private val nowMillis: () -> Long,
) : SimpleFeature<SeasonState, SeasonAction, SeasonEvent> by feature(SeasonState(), scope) {

    init {
        observeSeason()
        awaitActionsIn(scope) { action ->
            when (action) {
                SeasonAction.Retry -> observeSeason()
            }
        }
    }

    fun isOurs(round: SeasonRound, index: Int): Boolean {
        val match = round.matches.getOrNull(index) ?: return false
        return matchRepository.isOurs(match.home.name, match.away.name)
    }

    private fun observeSeason() {
        composeState(
            scope = scope,
            source = matchRepository.season().withInitial(scope, Loadable.Loading),
        ) { state, loadable ->
            when (loadable) {
                is Loadable.Loading -> state.copy(isLoading = true, error = null)
                is Loadable.Failed -> state.copy(isLoading = false, error = loadable.error)
                is Loadable.Content -> state.copy(
                    rounds = loadable.value.rounds.toImmutableList(),
                    initialRoundIndex = startingRoundIndex(loadable.value),
                    isLoading = false,
                    error = null,
                )
            }
        }
    }

    private fun startingRoundIndex(season: Season): Int {
        val current = matchRepository.currentRound(season, nowMillis())
        return season.rounds.indexOfFirst { it.number == current?.number }.coerceAtLeast(0)
    }
}
