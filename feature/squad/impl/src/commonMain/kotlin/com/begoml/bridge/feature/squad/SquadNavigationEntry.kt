package com.begoml.bridge.feature.squad

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.begoml.bridge.feature.squad.api.PlayerDetailRoute
import com.begoml.bridge.feature.squad.api.SquadRoute
import com.begoml.bridge.feature.squad.grid.SquadDelegate
import com.begoml.bridge.feature.squad.grid.SquadEvent
import com.begoml.bridge.feature.squad.grid.SquadScreen
import com.begoml.bridge.feature.squad.player.PlayerScreen
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

internal class SquadNavigationEntry(private val navigator: Navigator) : FeatureNavigationEntry {

    override fun register(scope: EntryProviderScope<NavKey>) {
        scope.entry<SquadRoute> {
            val coroutineScope: CoroutineScope = rememberCoroutineScope()
            val delegate: SquadDelegate = koinInject { parametersOf(coroutineScope) }

            LaunchedEffect(delegate) {
                delegate.singleEvents.collectLatest { event ->
                    when (event) {
                        is SquadEvent.OpenPlayer -> navigator.push(PlayerDetailRoute(event.playerId))
                    }
                }
            }

            SquadScreen(delegate = delegate)
        }

        // No swipe-back metadata: the pager owns the horizontal drag and the two would fight.
        scope.entry<PlayerDetailRoute> { route ->
            val coroutineScope: CoroutineScope = rememberCoroutineScope()
            val delegate: SquadDelegate = koinInject { parametersOf(coroutineScope) }
            PlayerScreen(
                delegate = delegate,
                initialPlayerId = route.playerId,
                onBack = navigator::pop,
            )
        }
    }
}
