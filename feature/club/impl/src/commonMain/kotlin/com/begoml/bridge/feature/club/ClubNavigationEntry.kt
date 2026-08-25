package com.begoml.bridge.feature.club

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.begoml.bridge.feature.club.api.ClubRoute
import com.begoml.bridge.navigation.FeatureNavigationEntry
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope

internal class ClubNavigationEntry : FeatureNavigationEntry {

    override fun register(scope: EntryProviderScope<NavKey>) {
        scope.entry<ClubRoute> {
            val coroutineScope: CoroutineScope = rememberCoroutineScope()
            val delegate: ClubDelegate = koinInject { parametersOf(coroutineScope) }
            ClubScreen(delegate = delegate)
        }
    }
}
