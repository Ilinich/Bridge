package com.begoml.bridge.foundation.tessera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class ScopedViewModel(scope: CoroutineScope) : ViewModel(scope)

/**
 * The guarantee every state holder in this app leans on.
 *
 * A ViewModel and the feature it drives share one scope, created outside both and handed to the
 * ViewModel; nothing in either of them cancels it. If this constructor ever stopped cancelling
 * what it was given, every feature would keep collecting after its screen was gone, and nothing
 * on screen would look wrong.
 */
class ViewModelScopeTest {

    @Test
    fun `clearing a view model cancels the scope it was handed`() {
        val scope = CoroutineScope(SupervisorJob())
        val store = ViewModelStore()
        val provider = ViewModelProvider.create(
            store = store,
            factory = viewModelFactory { initializer { ScopedViewModel(scope) } },
        )
        provider[ScopedViewModel::class]
        assertTrue(scope.isActive)

        // What the navigation host does when a screen is popped for good.
        store.clear()

        assertFalse(scope.isActive)
    }
}
