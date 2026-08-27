package com.begoml.bridge.feature.squad

import androidx.lifecycle.ViewModel
import com.begoml.bridge.core.data.repository.SquadRepository
import com.begoml.bridge.core.following.FollowingFeature
import com.begoml.bridge.core.analytics.Analytics
import com.begoml.bridge.feature.squad.analytics.PlayerOpened
import com.begoml.bridge.feature.player.api.PlayerDetailRoute
import com.begoml.bridge.feature.squad.grid.SquadDelegate
import com.begoml.bridge.feature.squad.grid.SquadEvent
import com.begoml.bridge.feature.squad.grid.SquadState
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateTo
import com.begoml.bridge.navigation.router.navigateUp
import com.begoml.bridge.foundation.tessera.UiStateDelegate
import com.begoml.bridge.foundation.tessera.UiStateDelegateImpl
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SquadUiState(
    val squad: SquadState = SquadState(),
    val followed: ImmutableSet<String> = persistentSetOf(),
)

/**
 * The squad grid.
 *
 * The delegate reports a tapped player as an event; turning that event into a destination happens
 * here, so the screen never names a route and never holds the router. Who is followed comes from
 * a feature shared with the rest of the app rather than from this screen's own state.
 */
internal class SquadViewModel(
    private val scope: CoroutineScope,
    private val delegate: SquadDelegate,
    private val following: FollowingFeature,
    private val router: AppRouter,
    private val analytics: Analytics,
) : ViewModel(), UiStateDelegate<SquadUiState, Nothing> by UiStateDelegateImpl(SquadUiState()) {

    init {
        scope.launch {
            combine(delegate.uiStateFlow, following.stateFlow) { squad, followed ->
                SquadUiState(squad = squad, followed = followed.playerIds)
            }.collect { built -> updateUiState { built } }
        }
        scope.launch {
            delegate.singleEvents.collect { event ->
                when (event) {
                    is SquadEvent.OpenPlayer -> {
                        analytics.track(PlayerOpened(event.playerId))
                        router.navigateTo(PlayerDetailRoute(event.playerId))
                    }
                }
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
    }

    fun onPlayerClick(playerId: String) {
        delegate.onPlayerClick(playerId)
    }

    fun retry() {
        delegate.retry()
    }
}
