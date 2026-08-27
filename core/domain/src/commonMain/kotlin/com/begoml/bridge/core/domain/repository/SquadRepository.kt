package com.begoml.bridge.core.domain.repository

import com.begoml.bridge.core.domain.model.Loadable
import com.begoml.bridge.core.domain.model.Player
import kotlinx.coroutines.flow.Flow

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
