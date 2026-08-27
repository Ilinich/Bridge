package com.begoml.bridge.core.domain.repository

import com.begoml.bridge.core.domain.model.Club
import com.begoml.bridge.core.domain.model.Loadable
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

    fun club(teamId: String): Flow<Loadable<Club>>

    fun venue(teamId: String): Flow<Loadable<Venue>>

    suspend fun refresh(teamId: String)
}
