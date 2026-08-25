package com.begoml.bridge.navigation.swipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Caps the background entry's lifecycle at [maxState] so RESUMED-only effects of the covered
 * previous screen do not run while it is used as a swipe background. */
internal class CappedLifecycleOwner(
    private val maxState: Lifecycle.State = Lifecycle.State.STARTED,
) : LifecycleOwner {
    private val registry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = registry

    fun sync(sourceState: Lifecycle.State) {
        if (registry.currentState == Lifecycle.State.DESTROYED) return
        registry.currentState = minOf(sourceState, maxState)
    }

    fun destroy() {
        if (registry.currentState != Lifecycle.State.DESTROYED) registry.currentState = Lifecycle.State.DESTROYED
    }
}

@Composable
internal fun rememberCappedLifecycleOwner(
    source: LifecycleOwner = LocalLifecycleOwner.current,
): LifecycleOwner {
    val owner = remember { CappedLifecycleOwner() }
    DisposableEffect(source) {
        owner.sync(source.lifecycle.currentState)
        val observer = LifecycleEventObserver { _, _ -> owner.sync(source.lifecycle.currentState) }
        source.lifecycle.addObserver(observer)
        onDispose {
            source.lifecycle.removeObserver(observer)
            owner.destroy()
        }
    }
    return owner
}
