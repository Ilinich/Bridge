package com.begoml.bridge.feature.squad

import androidx.lifecycle.ViewModel
import com.begoml.bridge.core.data.repository.SquadRepository
import com.begoml.bridge.core.following.FollowingFeature
import com.begoml.bridge.core.analytics.Analytics
import com.begoml.bridge.feature.squad.analytics.PlayerOpened
import com.begoml.bridge.feature.squad.api.PlayerDetailRoute
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
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
    val followed: ImmutableSet<String> = persistentSetOf(),
)

/** The player pager. It reads the same squad and owns nothing but the way back. */
internal class PlayerViewModel(
    private val scope: CoroutineScope,
    private val delegate: SquadDelegate,
    private val following: FollowingFeature,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), UiStateDelegate<PlayerUiState, Nothing> by UiStateDelegateImpl(PlayerUiState()) {

    init {
        scope.launch {
            val labels = withContext(ioDispatcher) { readLabels() }
            combine(delegate.uiStateFlow, following.stateFlow) { squad, followed ->
                PlayerUiState(squad = squad, labels = labels, followed = followed.playerIds)
            }.collect { built -> updateUiState { built } }
        }
    }

    fun onFollowClick(playerId: String) {
        following.toggle(playerId)
    }

    override fun onCleared() {
        scope.cancel()
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
