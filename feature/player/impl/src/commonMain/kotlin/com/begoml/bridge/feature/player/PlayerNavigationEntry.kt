package com.begoml.bridge.feature.player

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.begoml.bridge.feature.player.api.PlayerDetailRoute
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.swipeBackMetadata
import org.koin.compose.viewmodel.koinViewModel

internal class PlayerNavigationEntry : FeatureNavigationEntry {

    override fun register(scope: EntryProviderScope<NavKey>) {
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
