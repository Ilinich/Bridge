package com.begoml.bridge.foundation.resource

/**
 * What a screen has of a resource right now.
 *
 * [Failed] means the load failed **and** nothing is cached. A refresh that fails while a value is
 * already held keeps emitting [Content]: the screen shows what it has instead of replacing real
 * data with an error.
 */
sealed interface Loadable<out T> {

    data object Loading : Loadable<Nothing>

    data class Content<T>(val value: T) : Loadable<T>

    data class Failed(val error: Throwable) : Loadable<Nothing>
}

fun <T, R> Loadable<T>.map(transform: (T) -> R): Loadable<R> = when (this) {
    is Loadable.Loading -> Loadable.Loading
    is Loadable.Failed -> this
    is Loadable.Content -> Loadable.Content(transform(value))
}

val <T> Loadable<T>.valueOrNull: T?
    get() = (this as? Loadable.Content)?.value
