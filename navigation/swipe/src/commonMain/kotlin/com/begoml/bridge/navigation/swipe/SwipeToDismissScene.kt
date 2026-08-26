package com.begoml.bridge.navigation.swipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

internal data class SwipeToDismissScene(
    override val key: Any,
    val previousEntry: NavEntry<NavKey>,
    val currentEntry: NavEntry<NavKey>,
    override val previousEntries: List<NavEntry<NavKey>>,
    val freezeBackgroundWhileIdle: Boolean,
    val edgeWidthDp: Dp?,
    val swipeFromAnywhere: Boolean,
    val sensitivity: SwipeSensitivity,
    val onBack: () -> Unit,
) : Scene<NavKey> {

    override val entries: List<NavEntry<NavKey>> = listOf(currentEntry)

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
