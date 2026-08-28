package com.begoml.bridge.foundation.strings

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * Turns a resource id into the word it stands for.
 *
 * A feature passes ids from its own resources and never names the mechanism that reads them: the
 * lookup is suspending on both platforms — the words live in a bundle, not in a constant — and a
 * caller that knew this would be a caller that has to think about threads to ask for a noun.
 */
interface StringResolver {

    suspend fun get(id: StringResource): String

    suspend fun get(id: StringResource, vararg args: Any): String
}

/**
 * Resolves off the thread that draws, on the dispatcher it is handed.
 *
 * One implementation for the whole app, because the answer does not depend on who asks.
 */
class ComposeStringResolver(private val dispatcher: CoroutineDispatcher) : StringResolver {

    override suspend fun get(id: StringResource): String =
        withContext(dispatcher) { getString(id) }

    override suspend fun get(id: StringResource, vararg args: Any): String =
        withContext(dispatcher) { getString(id, *args) }
}
