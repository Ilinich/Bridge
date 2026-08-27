package com.begoml.bridge.core.domain.repository

import com.begoml.bridge.core.domain.model.Loadable
import com.begoml.bridge.core.domain.model.Match
import com.begoml.bridge.core.domain.model.Season
import com.begoml.bridge.core.domain.model.SeasonMatch
import kotlinx.coroutines.flow.Flow

/**
 * Fixtures, results and the season calendar.
 */
interface MatchRepository {

    fun nextMatch(teamId: String): Flow<Loadable<Match?>>

    fun lastResult(teamId: String): Flow<Loadable<Match?>>

    fun season(): Flow<Loadable<Season>>

    fun match(id: String): Flow<Loadable<SeasonMatch?>>

    /** Revalidates the calendar and the fixture caches now, ignoring their time to live. */
    suspend fun refresh()
}
