package com.begoml.bridge.feature.squad.grid

import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.core.domain.model.Player
import com.begoml.bridge.core.domain.model.FollowedClub
import com.begoml.bridge.core.domain.repository.SquadRepository
import com.begoml.bridge.foundation.tessera.UiStateDelegate
import com.begoml.bridge.foundation.tessera.UiStateDelegateImpl
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class SquadState(
    val players: ImmutableList<Player> = persistentListOf(),
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)

sealed interface SquadEvent {
    data class OpenPlayer(val playerId: String) : SquadEvent
}

/**
 * The squad list.
 *
 * One source, no composition, so this is a [UiStateDelegate] rather than a feature. A short squad
 * is a normal answer from the free tier and renders as a squad; only a failed load becomes an
 * error.
 */
class SquadDelegate(
    private val scope: CoroutineScope,
    private val repository: SquadRepository,
    private val club: FollowedClub,
) : UiStateDelegate<SquadState, SquadEvent> by UiStateDelegateImpl(SquadState()) {

    /** The subscription [retry] replaces, so repeated taps cannot stack collectors. */
    private var squadJob: Job? = null

    init {
        observe()
    }

    fun onPlayerClick(playerId: String) {
        scope.launch { sendEvent(SquadEvent.OpenPlayer(playerId)) }
    }

    fun retry() {
        observe()
    }

    private fun observe() {
        squadJob?.cancel()
        squadJob = scope.launch {
            repository.squad(club.id).collect { loadable ->
                updateUiState { state ->
                    when (loadable) {
                        is Loadable.Loading -> state.copy(isLoading = true, error = null)
                        is Loadable.Failed -> state.copy(isLoading = false, error = loadable.error)
                        is Loadable.Content -> state.copy(
                            players = loadable.value.toImmutableList(),
                            isLoading = false,
                            error = null,
                        )
                    }
                }
            }
        }
    }
}
