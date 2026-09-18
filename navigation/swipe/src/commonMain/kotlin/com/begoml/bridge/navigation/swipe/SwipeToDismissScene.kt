package com.begoml.bridge.navigation.swipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene

internal data class SwipeToDismissScene<T : Any>(
    override val key: Any,
    val previousEntry: NavEntry<T>,
    val currentEntry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
    val freezeBackgroundWhileIdle: Boolean,
    val edgeWidthDp: Dp?,
    val swipeFromAnywhere: Boolean,
    val sensitivity: SwipeSensitivity,
    val onBack: () -> Unit,
) : Scene<T> {

    override val entries: List<NavEntry<T>> = listOf(currentEntry)

    override val content: @Composable () -> Unit = {
        if (!LocalSwipeGestureAvailable.current) {
            currentEntry.Content()
        } else {
            SwipeToDismissLayout(
                onDismiss = onBack,
                backgroundContent = {
                    CompositionLocalProvider(LocalLifecycleOwner provides rememberCappedLifecycleOwner()) {
                        previousEntry.Content()
                    }
                },
                foregroundContent = { currentEntry.Content() },
                freezeBackgroundWhileIdle = freezeBackgroundWhileIdle,
                edgeWidthDp = edgeWidthDp,
                swipeFromAnywhere = swipeFromAnywhere,
                sensitivity = sensitivity,
            )
        }
    }
}
