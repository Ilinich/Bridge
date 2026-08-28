package com.begoml.bridge.feature.squad

import androidx.lifecycle.ViewModel
import com.begoml.bridge.core.analytics.Analytics
import com.begoml.bridge.core.domain.model.FollowedClub
import com.begoml.bridge.core.domain.model.Player
import com.begoml.bridge.core.domain.repository.SquadRepository
import com.begoml.bridge.core.features.following.FollowingFeature
import com.begoml.bridge.feature.player.api.PlayerDetailRoute
import com.begoml.bridge.feature.squad.analytics.PlayerOpened
import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.foundation.tessera.UiStateDelegate
import com.begoml.bridge.foundation.tessera.UiStateDelegateImpl
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateTo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A card in the grid, with everything the card draws and nothing else. */
data class PlayerCardUi(
    val id: String,
    val name: String,
    val position: String?,
    val shirtNumber: String?,
    val cutoutUrl: String?,
    val followed: Boolean,
)

data class SquadUiState(
    val players: ImmutableList<PlayerCardUi> = persistentListOf(),
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)

/**
 * The squad grid.
 *
 * It reads the squad itself: a state holder in between would hold a copy of this state and forward
 * every call, and there is no second reader to justify one. A short squad is a normal answer from
 * the free tier and renders as a squad; only a failed load becomes an error.
 *
 * Who is followed comes from a feature shared with the rest of the app, and is folded into the
 * cards here so that the grid does not look an id up per item while it composes.
 */
internal class SquadViewModel(
    private val scope: CoroutineScope,
    private val repository: SquadRepository,
    private val club: FollowedClub,
    private val following: FollowingFeature,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
    private val analytics: Analytics,
) : ViewModel(), UiStateDelegate<SquadUiState, Nothing> by UiStateDelegateImpl(SquadUiState()) {

    init {
        scope.launch {
            combine(repository.squad(club.id), following.stateFlow) { loadable, followed ->
                loadable to followed.playerIds
            }.collect { (loadable, followed) ->
                val built = withContext(ioDispatcher) { toUiState(loadable, followed) }
                updateUiState { built }
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
    }

    fun onPlayerClick(playerId: String) {
        analytics.track(PlayerOpened(playerId))
        router.navigateTo(PlayerDetailRoute(playerId))
    }

    fun retry() {
        scope.launch { repository.refresh(club.id) }
    }

    private fun toUiState(
        loadable: Loadable<List<Player>>,
        followed: Set<String>,
    ): SquadUiState = when (loadable) {
        is Loadable.Loading -> SquadUiState(isLoading = true)
        is Loadable.Failed -> SquadUiState(isLoading = false, error = loadable.error)
        is Loadable.Content -> SquadUiState(
            players = loadable.value.map { player -> player.toCardUi(followed) }.toImmutableList(),
            isLoading = false,
        )
    }

    private fun Player.toCardUi(followed: Set<String>) = PlayerCardUi(
        id = id,
        name = name,
        position = position,
        shirtNumber = shirtNumber,
        cutoutUrl = cutoutUrl,
        followed = id in followed,
    )
}
