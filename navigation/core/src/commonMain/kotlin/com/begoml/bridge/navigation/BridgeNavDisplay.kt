package com.begoml.bridge.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.compose.ui.unit.Dp
import com.begoml.bridge.navigation.swipe.FreezeBackgroundWhileIdle
import com.begoml.bridge.navigation.swipe.SwipeDismissSensitivity
import com.begoml.bridge.navigation.swipe.SwipeEdgeGate
import com.begoml.bridge.navigation.swipe.SwipeSensitivity
import com.begoml.bridge.navigation.swipe.SwipeToDismissSceneStrategy

private const val PushDurationMillis = 260
private const val FadeDurationMillis = 180
private const val EnterOffsetFraction = 0.12f
private const val ExitOffsetFraction = 0.06f

/**
 * The navigation surface.
 *
 * Pushes slide in from the right; a swipe from the left edge drags the top screen away with the
 * finger and hands the gesture to [SwipeToDismissSceneStrategy]. A tab switch replaces the whole
 * back stack, so it runs through the same push transition — there is no separate tab animation.
 */
@Composable
fun BridgeNavDisplay(
    backStack: List<Route>,
    onBack: () -> Unit,
    entries: List<FeatureNavigationEntry>,
    modifier: Modifier = Modifier,
) {
    val entryProvider = entryProvider<NavKey> {
        entries.forEach { entry -> entry.register(this) }
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = onBack,
        // Without these a screen's ViewModel would live in the host's store: shared between
        // destinations and never cleared when one is popped.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        sceneStrategies = listOf(SwipeToDismissSceneStrategy()),
        entryProvider = entryProvider,
        transitionSpec = {
            slideInHorizontally(tween(PushDurationMillis)) { width ->
                (width * EnterOffsetFraction).toInt()
            }.plus(fadeIn(tween(FadeDurationMillis))) togetherWith
                slideOutHorizontally(tween(PushDurationMillis)) { width ->
                    -(width * ExitOffsetFraction).toInt()
                }.plus(fadeOut(tween(FadeDurationMillis)))
        },
        popTransitionSpec = {
            slideInHorizontally(tween(PushDurationMillis)) { width ->
                -(width * ExitOffsetFraction).toInt()
            }.plus(fadeIn(tween(FadeDurationMillis))) togetherWith
                slideOutHorizontally(tween(PushDurationMillis)) { width ->
                    (width * EnterOffsetFraction).toInt()
                }.plus(fadeOut(tween(FadeDurationMillis)))
        },
    )
}

/**
 * Marks an entry as draggable back. Screens that own a horizontal gesture must not use it.
 *
 * The gesture reads its settings from this metadata, so anything not passed here is unreachable at
 * runtime: the defaults are what every entry gets unless it says otherwise.
 */
fun swipeBackMetadata(
    sensitivity: SwipeSensitivity = SwipeSensitivity.Default,
    edgeWidth: Dp? = null,
    swipeFromAnywhere: Boolean = false,
    freezeBackgroundWhileIdle: Boolean = true,
): Map<String, Any> = SwipeToDismissSceneStrategy.enabled() +
    SwipeDismissSensitivity.metadata(sensitivity) +
    SwipeEdgeGate.metadata(edgeWidthDp = edgeWidth, swipeFromAnywhere = swipeFromAnywhere) +
    (if (freezeBackgroundWhileIdle) FreezeBackgroundWhileIdle.enabled() else emptyMap())
