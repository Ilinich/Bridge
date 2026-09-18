package com.begoml.bridge.feature.squad

import androidx.navigation3.runtime.EntryProviderScope
import com.begoml.bridge.navigation.Route
import com.begoml.bridge.feature.squad.api.SquadRoute
import com.begoml.bridge.feature.squad.grid.SquadScreen
import com.begoml.bridge.navigation.FeatureNavigationEntry
import org.koin.compose.viewmodel.koinViewModel

internal class SquadNavigationEntry : FeatureNavigationEntry {

    override fun register(scope: EntryProviderScope<Route>) {
        scope.entry<SquadRoute> {
            SquadScreen(viewModel = koinViewModel<SquadViewModel>())
        }
    }
}
