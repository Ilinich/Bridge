package com.begoml.bridge.core.data.repository

import com.begoml.bridge.foundation.resource.persistedResource
import com.begoml.bridge.core.domain.repository.SquadRepository

import com.begoml.bridge.core.data.db.PlayerStore
import com.begoml.bridge.core.data.db.Syncer
import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.core.domain.model.Player
import com.begoml.bridge.core.data.mapper.toPlayer
import com.begoml.bridge.core.data.remote.sportsdb.SportsDbApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.hours

internal class SquadRepositoryImpl(
    private val api: SportsDbApi,
    private val store: PlayerStore,
    private val syncer: Syncer,
    private val dispatcher: CoroutineDispatcher,
) : SquadRepository {

    // flowOn on the whole chain: mapping a whole squad has no business on the context that
    // collects it, and neither has the fetch persistedResource starts.
    override fun squad(teamId: String): Flow<Loadable<List<Player>>> = persistedResource(
        stored = store.observeAll().map { players -> players.takeIf { it.isNotEmpty() } },
    ) {
        syncer.sync(key = squadKey(teamId), ttl = 4.hours) { fetch(teamId) }
    }.flowOn(dispatcher)

    override suspend fun refresh(teamId: String) = withContext(dispatcher) {
        syncer.sync(key = squadKey(teamId), ttl = null, force = true) { fetch(teamId) }
    }

    private fun squadKey(teamId: String) = "squad:$teamId"

    private suspend fun fetch(teamId: String) {
        val players = api.squad(teamId).mapNotNull { it.toPlayer() }
        if (players.isNotEmpty()) {
            store.replaceAll(players)
        }
    }
}
