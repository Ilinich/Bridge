package com.begoml.bridge.feature.player

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.begoml.bridge.feature.player.api.PlayerDetailRoute
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.swipeBackMetadata
import org.koin.compose.viewmodel.koinViewModel

internal class PlayerNavigationEntry : FeatureNavigationEntry {

    override fun register(scope: EntryProviderScope<NavKey>) {
        scope.entry<PlayerDetailRoute>(metadata = swipeBackMetadata()) { route ->
            PlayerScreen(
                viewModel = koinViewModel<PlayerViewModel>(),
                initialPlayerId = route.playerId,
            )
        }
    }
}
