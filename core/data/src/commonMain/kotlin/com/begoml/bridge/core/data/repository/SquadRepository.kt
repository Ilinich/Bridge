package com.begoml.bridge.core.data.repository

import com.begoml.bridge.core.data.db.PlayerDao
import com.begoml.bridge.core.data.db.Syncer
import com.begoml.bridge.core.data.db.toEntity
import com.begoml.bridge.core.data.db.toPlayer
import com.begoml.bridge.core.data.model.Loadable
import com.begoml.bridge.core.data.model.Player
import com.begoml.bridge.core.data.mapper.toPlayer
import com.begoml.bridge.core.data.sportsdb.SportsDbApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.hours

/**
 * The squad, held on disk for four hours before it is revalidated.
 *
 * A squad changes on transfer days, not on minutes, so a cold start draws the stored one
 * immediately and the network only confirms it.
 */
interface SquadRepository {

    fun squad(teamId: String): Flow<Loadable<List<Player>>>

    /** Revalidates now, ignoring the time to live. */
    suspend fun refresh(teamId: String)
}

internal class SquadRepositoryImpl(
    private val api: SportsDbApi,
    private val dao: PlayerDao,
    private val syncer: Syncer,
    private val dispatcher: CoroutineDispatcher,
) : SquadRepository {

    override fun squad(teamId: String): Flow<Loadable<List<Player>>> = persistedResource(
        // flowOn, because map runs in the collector's context and the collector is the main
        // dispatcher: mapping a whole squad would otherwise land on the frame that draws it.
        stored = dao.observeAll().map { rows ->
            rows.takeIf { it.isNotEmpty() }?.map { it.toPlayer() }
        }.flowOn(dispatcher),
    ) {
        syncer.sync(key = squadKey(teamId), ttl = 4.hours) { fetch(teamId) }
    }

    override suspend fun refresh(teamId: String) = withContext(dispatcher) {
        syncer.sync(key = squadKey(teamId), ttl = null, force = true) { fetch(teamId) }
    }

    private fun squadKey(teamId: String) = "squad:$teamId"

    private suspend fun fetch(teamId: String) {
        val players = api.squad(teamId).mapNotNull { it.toPlayer() }
        if (players.isNotEmpty()) {
            dao.replaceAll(players.mapIndexed { index, player -> player.toEntity(index) })
        }
    }
}
