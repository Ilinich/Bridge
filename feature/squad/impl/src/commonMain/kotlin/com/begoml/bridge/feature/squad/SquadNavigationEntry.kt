package com.begoml.bridge.feature.squad

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.begoml.bridge.feature.squad.api.PlayerDetailRoute
import com.begoml.bridge.feature.squad.api.SquadRoute
import com.begoml.bridge.feature.squad.grid.SquadScreen
import com.begoml.bridge.feature.squad.player.PlayerScreen
import com.begoml.bridge.navigation.FeatureNavigationEntry
import org.koin.compose.viewmodel.koinViewModel

internal class SquadNavigationEntry : FeatureNavigationEntry {

    override fun register(scope: EntryProviderScope<NavKey>) {
        scope.entry<SquadRoute> {
            SquadScreen(viewModel = koinViewModel<SquadViewModel>())
        }

        // No swipe-back metadata: the pager owns the horizontal drag and the two would fight.
        scope.entry<PlayerDetailRoute> { route ->
            PlayerScreen(
                viewModel = koinViewModel<PlayerViewModel>(),
                initialPlayerId = route.playerId,
            )
        }
    }
}
