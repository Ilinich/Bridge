package com.begoml.bridge.feature.club

import androidx.navigation3.runtime.EntryProviderScope
import com.begoml.bridge.navigation.Route
import com.begoml.bridge.feature.club.api.ClubRoute
import com.begoml.bridge.navigation.FeatureNavigationEntry
import org.koin.compose.viewmodel.koinViewModel

internal class ClubNavigationEntry : FeatureNavigationEntry {

    override fun register(scope: EntryProviderScope<Route>) {
        scope.entry<ClubRoute> {
            ClubScreen(viewModel = koinViewModel<ClubViewModel>())
        }
    }
}
