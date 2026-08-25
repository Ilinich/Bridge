package com.begoml.bridge.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.begoml.bridge.navigation.swipe.SwipeToDismissSceneStrategy

private const val PushDurationMillis = 260
private const val FadeDurationMillis = 180
private const val EnterOffsetFraction = 0.12f
private const val ExitOffsetFraction = 0.06f

/**
 * The navigation surface.
 *
 * Pushes slide in from the right; a swipe from the left edge drags the top screen away with the
 * finger and hands the gesture to [SwipeToDismissSceneStrategy]. Tab switches are handled by
 * [TabbedBackStack] above this and cross-fade rather than slide, because tabs are not ordered in
 * space and a slide would imply they were.
 */
@Composable
fun BridgeNavDisplay(
    backStack: List<Route>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = onBack,
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

/** Marks an entry as draggable back. Screens that own a horizontal gesture must not use it. */
fun swipeBackMetadata(): Map<String, Any> = SwipeToDismissSceneStrategy.enabled()
