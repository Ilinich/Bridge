package com.begoml.bridge.foundation.tessera

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Runs the action-consumer loop, keeping it alive across a failing handler.
 *
 * A thrown handler would otherwise cancel the loop and leave the screen inert — taps arrive and
 * nothing happens, with no crash to point at. Cancellation is rethrown so the scope still tears
 * down normally; [onError] receives everything else.
 */
@Suppress("TooGenericExceptionCaught")
fun <Action> FeatureActionDelegate<Action>.awaitActionsIn(
    scope: CoroutineScope,
    onError: (Throwable) -> Unit = {},
    onAction: suspend (Action) -> Unit,
): Job = scope.launch {
    awaitActions { action ->
        try {
            onAction(action)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            onError(error)
        }
    }
}

fun <Event> FeatureEventDelegate<Event>.handleEventsIn(
    scope: CoroutineScope,
    onEvent: suspend (Event) -> Unit,
): Job = scope.launch { events.collect(onEvent) }
