package com.begoml.bridge.core.data.repository

import com.begoml.bridge.core.data.model.Club
import com.begoml.bridge.core.data.model.Loadable
import com.begoml.bridge.core.data.model.toClub
import com.begoml.bridge.core.data.sportsdb.SportsDbApi
import com.begoml.bridge.foundation.cache.InMemoryCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class ClubRepository internal constructor(
    private val teamId: String,
    api: SportsDbApi,
    dispatcher: CoroutineDispatcher,
    backgroundScope: CoroutineScope,
    nowMillis: () -> Long,
) {

    private val cache = InMemoryCache<String, Club>(
        loader = { id -> requireNotNull(api.team(id)?.toClub()) { "No club for $id" } },
        dispatcher = dispatcher,
        nowMillis = nowMillis,
        staleAfter = 24.hours,
        expireAfter = 7.days,
        backgroundScope = backgroundScope,
    )

    fun club(): Flow<Loadable<Club>> = cache.loadable(teamId)

    suspend fun refresh() {
        cache.refresh(teamId)
    }
}
