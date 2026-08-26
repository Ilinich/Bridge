package com.begoml.bridge.feature.squad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.begoml.bridge.core.data.repository.SquadRepository
import com.begoml.bridge.feature.squad.api.PlayerDetailRoute
import com.begoml.bridge.feature.squad.grid.SquadDelegate
import com.begoml.bridge.feature.squad.grid.SquadEvent
import com.begoml.bridge.feature.squad.grid.SquadState
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateTo
import com.begoml.bridge.navigation.router.navigateUp
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * The squad grid.
 *
 * The delegate reports a tapped player as an event; turning that event into a destination happens
 * here, so the screen never names a route and never holds the router.
 */
internal class SquadViewModel(
    repository: SquadRepository,
    private val router: AppRouter,
) : ViewModel() {

    private val delegate = SquadDelegate(scope = viewModelScope, repository = repository)

    val state: StateFlow<SquadState> = delegate.uiStateFlow

    init {
        viewModelScope.launch {
            delegate.singleEvents.collect { event ->
                when (event) {
                    is SquadEvent.OpenPlayer -> router.navigateTo(PlayerDetailRoute(event.playerId))
                }
            }
        }
    }

    fun onPlayerClick(playerId: String) {
        delegate.onPlayerClick(playerId)
    }

    fun retry() {
        delegate.retry()
    }
}

/** The player pager. It reads the same squad and owns nothing but the way back. */
internal class PlayerViewModel(
    repository: SquadRepository,
    private val router: AppRouter,
) : ViewModel() {

    private val delegate = SquadDelegate(scope = viewModelScope, repository = repository)

    val state: StateFlow<SquadState> = delegate.uiStateFlow

    fun onBack() {
        router.navigateUp()
    }
}
