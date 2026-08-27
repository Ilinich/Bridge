package com.begoml.bridge.feature.player

import androidx.lifecycle.ViewModel
import bridge.feature.player.impl.generated.resources.Res
import bridge.feature.player.impl.generated.resources.player_back
import bridge.feature.player.impl.generated.resources.player_country
import bridge.feature.player.impl.generated.resources.player_height
import bridge.feature.player.impl.generated.resources.player_not_found
import bridge.feature.player.impl.generated.resources.player_number
import bridge.feature.player.impl.generated.resources.player_position
import bridge.feature.player.impl.generated.resources.player_title
import com.begoml.bridge.core.following.FollowingFeature
import com.begoml.bridge.foundation.tessera.UiStateDelegate
import com.begoml.bridge.foundation.tessera.UiStateDelegateImpl
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateUp
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString

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
    val squad: PlayerSquadState = PlayerSquadState(),
    val labels: PlayerLabels? = null,
    val followed: ImmutableSet<String> = persistentSetOf(),
)

/** The player pager. It reads the squad, writes who is followed, and owns the way back. */
internal class PlayerViewModel(
    private val scope: CoroutineScope,
    private val delegate: PlayerDelegate,
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

    override fun onCleared() {
        scope.cancel()
    }

    fun onFollowClick(playerId: String) {
        following.toggle(playerId)
    }

    fun onBack() {
        router.navigateUp()
    }

    private suspend fun readLabels() = PlayerLabels(
        title = getString(Res.string.player_title),
        back = getString(Res.string.player_back),
        notFound = getString(Res.string.player_not_found),
        number = getString(Res.string.player_number),
        position = getString(Res.string.player_position),
        country = getString(Res.string.player_country),
        height = getString(Res.string.player_height),
    )
}
