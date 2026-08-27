package com.begoml.bridge.feature.player

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
import kotlinx.coroutines.launch

data class PlayerSquadState(
    val players: ImmutableList<Player> = persistentListOf(),
    val isLoading: Boolean = true,
)

/**
 * The squad, as this screen needs it.
 *
 * The profile is a pager over the whole squad rather than a card for one player, so it reads the
 * same repository the grid does. It reads it directly instead of borrowing the grid's state
 * holder: the two screens want different things from the same source — the grid needs a failure to
 * report, while a pager that has nothing to page has nothing to say either way.
 */
class PlayerDelegate(
    scope: CoroutineScope,
    repository: SquadRepository,
    club: FollowedClub,
) : UiStateDelegate<PlayerSquadState, Nothing> by UiStateDelegateImpl(PlayerSquadState()) {

    init {
        scope.launch {
            repository.squad(club.id).collect { loadable ->
                updateUiState { state ->
                    when (loadable) {
                        is Loadable.Loading -> state.copy(isLoading = true)
                        is Loadable.Failed -> state.copy(isLoading = false)
                        is Loadable.Content -> PlayerSquadState(
                            players = loadable.value.toImmutableList(),
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }
}
