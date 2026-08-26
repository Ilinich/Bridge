package com.begoml.bridge.navigation.swipe

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent

/**
 * Registers a NavDisplay entry that slides in on push and is dismissed by the horizontal
 * swipe-to-dismiss gesture (via [SwipeToDismissSceneStrategy]) on pop. Mirrors the app's
 * swipeToDismissHorizontalEntry.
 */
inline fun <reified T : NavKey> EntryProviderScope<NavKey>.swipeToDismissHorizontalEntry(
    edgeWidthDp: Dp? = null,
    swipeFromAnywhere: Boolean = false,
    sensitivity: SwipeSensitivity = SwipeSensitivity.Default,
    freezeBackgroundWhileIdle: Boolean = true,
    pushDurationMs: Int = 300,
    noinline content: @Composable (T) -> Unit,
) {
    val transitions = NavDisplay.transitionSpec {
        slideIntoContainer(SlideDirection.Left, tween(pushDurationMs, easing = LinearEasing)) togetherWith
            slideOutOfContainer(SlideDirection.Left, tween(pushDurationMs, easing = LinearEasing))
    } + NavDisplay.popTransitionSpec {
        ContentTransform(EnterTransition.None, ExitTransition.None)
    } + NavDisplay.predictivePopTransitionSpec(predictiveBackHorizontalTransition)
    val metadata = transitions +
        SwipeToDismissSceneStrategy.enabled() +
        SwipeEdgeGate.metadata(edgeWidthDp = edgeWidthDp, swipeFromAnywhere = swipeFromAnywhere) +
        SwipeDismissSensitivity.metadata(sensitivity) +
        (if (freezeBackgroundWhileIdle) FreezeBackgroundWhileIdle.enabled() else emptyMap())
    entry<T>(metadata = metadata, content = content)
}

/**
 * Predictive-back pop transition mirroring the Android app: the dismissed screen slides towards
 * the gesture edge while scaling down, the previous screen is revealed statically underneath.
 */
val predictiveBackHorizontalTransition:
    AnimatedContentTransitionScope<Scene<*>>.(swipeEdge: Int) -> ContentTransform = { edge ->
    val xSign = if (edge == NavigationEvent.EDGE_RIGHT) -1 else 1
    val moveSpec = tween<IntOffset>(durationMillis = 220, easing = FastOutSlowInEasing)
    val exiting = slideOut(
        animationSpec = moveSpec,
        targetOffset = { fullSize -> IntOffset(x = xSign * fullSize.width, y = 0) },
    )
    ContentTransform(
        targetContentEnter = EnterTransition.None,
        initialContentExit = exiting,
        sizeTransform = SizeTransform(clip = true),
        targetContentZIndex = -1f,
    )
}
