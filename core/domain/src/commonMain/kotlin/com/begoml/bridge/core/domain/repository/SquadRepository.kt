package com.begoml.bridge.core.domain.repository

import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.core.domain.model.Player
import kotlinx.coroutines.flow.Flow

/**
 * The squad, held on disk for four hours before it is revalidated.
 *
 * A squad changes on transfer days, not on minutes, so a cold start draws the stored one
 * immediately and the network only confirms it.
 */
interface SquadRepository {

    /**
     * The players, in the order the feed lists them.
     *
     * A short squad is a normal answer and arrives as [Loadable.Content]: the free tier of the
     * feed returns whoever it has, and a screen must not report that as a failure. The list is
     * replaced wholesale on each fetch, so a transferred player disappears rather than lingering.
     *
     * @param teamId the club's id in the upstream feed, never a name.
     */
    fun squad(teamId: String): Flow<Loadable<List<Player>>>

    /**
     * Fetches the squad now, however fresh it is, and returns when the work is done.
     *
     * The new list arrives through [squad] like any other change. Switches to the io dispatcher
     * itself, whatever the caller's context.
     */
    suspend fun refresh(teamId: String)
}
