package com.begoml.bridge.feature.squad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.begoml.bridge.core.data.repository.SquadRepository
import com.begoml.bridge.foundation.analytics.Analytics
import com.begoml.bridge.foundation.analytics.AnalyticsEvent
import com.begoml.bridge.feature.squad.api.PlayerDetailRoute
import com.begoml.bridge.feature.squad.grid.SquadDelegate
import com.begoml.bridge.feature.squad.grid.SquadEvent
import com.begoml.bridge.feature.squad.grid.SquadState
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateTo
import com.begoml.bridge.navigation.router.navigateUp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import bridge.feature.squad.impl.generated.resources.Res
import bridge.feature.squad.impl.generated.resources.squad_back
import bridge.feature.squad.impl.generated.resources.squad_country
import bridge.feature.squad.impl.generated.resources.squad_height
import bridge.feature.squad.impl.generated.resources.squad_not_found
import bridge.feature.squad.impl.generated.resources.squad_number
import bridge.feature.squad.impl.generated.resources.squad_position
import bridge.feature.squad.impl.generated.resources.squad_title

/**
 * The squad grid.
 *
 * The delegate reports a tapped player as an event; turning that event into a destination happens
 * here, so the screen never names a route and never holds the router.
 */
/** How long a state flow outlives its last collector, so a rotation does not refetch. */
private const val SubscriptionTimeoutMillis = 5_000L

internal class SquadViewModel(
    repository: SquadRepository,
    private val router: AppRouter,
    private val analytics: Analytics,
) : ViewModel() {

    private val delegate = SquadDelegate(scope = viewModelScope, repository = repository)

    val state: StateFlow<SquadState> = delegate.uiStateFlow

    init {
        viewModelScope.launch {
            delegate.singleEvents.collect { event ->
                when (event) {
                    is SquadEvent.OpenPlayer -> {
                        analytics.track(AnalyticsEvent.PlayerOpened(event.playerId))
                        router.navigateTo(PlayerDetailRoute(event.playerId))
                    }
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

/** Every fixed word on the player pager, resolved once away from the composition. */
data class PlayerLabels(
    val title: String,
    val back: String,
    val notFound: String,
    val number: String,
    val position: String,
    val country: String,
    val height: String,
)

data class PlayerUiState(
    val squad: SquadState = SquadState(),
    val labels: PlayerLabels? = null,
)

/** The player pager. It reads the same squad and owns nothing but the way back. */
internal class PlayerViewModel(
    repository: SquadRepository,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val delegate = SquadDelegate(scope = viewModelScope, repository = repository)
    private val labels = MutableStateFlow<PlayerLabels?>(null)

    val state: StateFlow<PlayerUiState> =
        combine(delegate.uiStateFlow, labels) { squad, resolved ->
            PlayerUiState(squad = squad, labels = resolved)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SubscriptionTimeoutMillis), PlayerUiState())

    init {
        viewModelScope.launch { labels.value = withContext(ioDispatcher) { readLabels() } }
    }

    fun onBack() {
        router.navigateUp()
    }

    private suspend fun readLabels() = PlayerLabels(
        title = getString(Res.string.squad_title),
        back = getString(Res.string.squad_back),
        notFound = getString(Res.string.squad_not_found),
        number = getString(Res.string.squad_number),
        position = getString(Res.string.squad_position),
        country = getString(Res.string.squad_country),
        height = getString(Res.string.squad_height),
    )
}
