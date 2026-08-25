package com.begoml.bridge.core.data.repository

import com.begoml.bridge.core.data.model.Loadable
import com.begoml.bridge.core.data.model.Player
import com.begoml.bridge.core.data.model.toPlayer
import com.begoml.bridge.core.data.sportsdb.SportsDbApi
import com.begoml.bridge.foundation.cache.InMemoryCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.hours

class SquadRepository internal constructor(
    private val teamId: String,
    api: SportsDbApi,
    dispatcher: CoroutineDispatcher,
    backgroundScope: CoroutineScope,
    nowMillis: () -> Long,
) {

    private val cache = InMemoryCache<String, List<Player>>(
        loader = { id -> api.squad(id).mapNotNull { it.toPlayer() } },
        dispatcher = dispatcher,
        nowMillis = nowMillis,
        staleAfter = 6.hours,
        expireAfter = 24.hours,
        backgroundScope = backgroundScope,
    )

    fun squad(): Flow<Loadable<List<Player>>> = cache.loadable(teamId)

    fun player(id: String): Player? = cache.peek(teamId)?.firstOrNull { it.id == id }
}
