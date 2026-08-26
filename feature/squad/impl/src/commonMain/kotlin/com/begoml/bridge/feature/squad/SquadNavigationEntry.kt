package com.begoml.bridge.feature.squad

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.begoml.bridge.feature.squad.api.PlayerDetailRoute
import com.begoml.bridge.feature.squad.api.SquadRoute
import com.begoml.bridge.feature.squad.grid.SquadScreen
import com.begoml.bridge.feature.squad.player.PlayerScreen
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.swipeBackMetadata
import org.koin.compose.viewmodel.koinViewModel

internal class SquadNavigationEntry : FeatureNavigationEntry {

    override fun register(scope: EntryProviderScope<NavKey>) {
        scope.entry<SquadRoute> {
            SquadScreen(viewModel = koinViewModel<SquadViewModel>())
        }

        // The pager owns the horizontal drag, but only while it has somewhere to go: on the first
        // player it stops consuming, and the leftover reaches the dismiss layer through nested
        // scroll. So the gesture closes the screen exactly where paging runs out.
        scope.entry<PlayerDetailRoute>(metadata = swipeBackMetadata()) { route ->
            PlayerScreen(
                viewModel = koinViewModel<PlayerViewModel>(),
                initialPlayerId = route.playerId,
            )
        }
    }
}
