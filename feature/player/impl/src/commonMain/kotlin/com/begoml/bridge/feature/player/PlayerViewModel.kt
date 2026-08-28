package com.begoml.bridge.feature.player

import com.begoml.bridge.foundation.logger.Logger
import com.begoml.bridge.foundation.coroutines.safeLaunch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bridge.feature.player.impl.generated.resources.Res
import bridge.feature.player.impl.generated.resources.player_back
import bridge.feature.player.impl.generated.resources.player_country
import bridge.feature.player.impl.generated.resources.player_height
import bridge.feature.player.impl.generated.resources.player_not_found
import bridge.feature.player.impl.generated.resources.player_number
import bridge.feature.player.impl.generated.resources.player_position
import bridge.feature.player.impl.generated.resources.player_title
import com.begoml.bridge.core.domain.model.FollowedClub
import com.begoml.bridge.core.domain.model.Player
import com.begoml.bridge.core.domain.repository.SquadRepository
import com.begoml.bridge.core.features.following.FollowingFeature
import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.foundation.tessera.UiStateDelegate
import com.begoml.bridge.foundation.tessera.UiStateDelegateImpl
import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.navigateUp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import org.jetbrains.compose.resources.getString

private const val Tag = "Player"

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

/** A page in the pager, with everything the page draws and nothing else. */
data class PlayerPageUi(
    val id: String,
    val name: String,
    val shirtNumber: String?,
    val position: String?,
    val nationality: String?,
    val height: String?,
    val cutoutUrl: String?,
    val followed: Boolean,
)

data class PlayerUiState(
    val players: ImmutableList<PlayerPageUi> = persistentListOf(),
    val isLoading: Boolean = true,
)

/** The player pager. It reads the squad, writes who is followed, and owns the way back. */
internal class PlayerViewModel(
    scope: CoroutineScope,
    private val repository: SquadRepository,
    private val club: FollowedClub,
    private val following: FollowingFeature,
    val labels: PlayerLabels,
    private val router: AppRouter,
    private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger,
) : ViewModel(scope), UiStateDelegate<PlayerUiState> by UiStateDelegateImpl(PlayerUiState()) {

    init {
        viewModelScope.safeLaunch(dispatcher = ioDispatcher, logger = logger, tag = Tag) {
            combine(repository.squad(club.id), following.stateFlow) { loadable, followed ->
                loadable to followed.playerIds
            }.collect { (loadable, followed) ->
                val built = toUiState(loadable, followed)
                updateUiState { built }
            }
        }
    }


    fun onFollowClick(playerId: String) {
        following.toggle(playerId)
    }

    fun onBack() {
        router.navigateUp()
    }

    private fun toUiState(
        loadable: Loadable<List<Player>>,
        followed: Set<String>,
    ): PlayerUiState = when (loadable) {
        is Loadable.Content -> PlayerUiState(
            players = loadable.value.map { player -> player.toPageUi(followed) }.toImmutableList(),
            isLoading = false,
        )
        is Loadable.Loading -> PlayerUiState(isLoading = true)
        // A pager with nothing to page has nothing to say either way, so a failure reads the same
        // as an empty squad: the screen states that the player is not loaded, and offers no retry
        // it could not honour — the squad is fetched by the grid this screen was opened from.
        is Loadable.Failed -> PlayerUiState(isLoading = false)
    }

    private fun Player.toPageUi(followed: Set<String>) = PlayerPageUi(
        id = id,
        name = name,
        shirtNumber = shirtNumber,
        position = position,
        nationality = nationality,
        height = height,
        cutoutUrl = cutoutUrl,
        followed = id in followed,
    )

}

/** Read once for the whole run: the words do not change while the app is open. */
suspend fun loadPlayerLabels() = PlayerLabels(
    title = getString(Res.string.player_title),
    back = getString(Res.string.player_back),
    notFound = getString(Res.string.player_not_found),
    number = getString(Res.string.player_number),
    position = getString(Res.string.player_position),
    country = getString(Res.string.player_country),
    height = getString(Res.string.player_height),
)
