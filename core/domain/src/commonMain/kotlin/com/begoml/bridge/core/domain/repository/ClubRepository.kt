package com.begoml.bridge.core.domain.repository

import com.begoml.bridge.core.domain.model.Club
import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.core.domain.model.Venue
import kotlinx.coroutines.flow.Flow

/**
 * The club profile and the ground it plays on.
 *
 * Both change about once a year, so both are held for a week. The venue is a second request that
 * only the club screen needs, and it is only made once the club record has told us which ground
 * to ask about.
 */
interface ClubRepository {

    /**
     * The club, drawn from disk first and confirmed over the network.
     *
     * Emits [Loadable.Loading] until something is known, and keeps emitting [Loadable.Content]
     * afterwards: a later revalidation that fails does not replace a club already held.
     *
     * @param teamId the club's id in the upstream feed, never a name.
     */
    fun club(teamId: String): Flow<Loadable<Club>>

    /**
     * The ground the club plays on.
     *
     * Chained behind [club], because only the club record says which ground to ask about: this
     * stays [Loadable.Loading] until that answer arrives, and a club with no ground on file never
     * leaves it.
     */
    fun venue(teamId: String): Flow<Loadable<Venue>>

    /**
     * Fetches both now, however fresh they are, and returns when the work is done.
     *
     * It returns nothing because the new values arrive through [club] and [venue] like any other
     * change. Switches to the io dispatcher itself, whatever the caller's context.
     */
    suspend fun refresh(teamId: String)
}
