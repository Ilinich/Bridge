package com.begoml.bridge.core.data.repository

import com.begoml.bridge.core.data.db.ClubDao
import com.begoml.bridge.core.data.db.Syncer
import com.begoml.bridge.core.data.db.toClub
import com.begoml.bridge.core.data.db.toEntity
import com.begoml.bridge.core.data.model.Club
import com.begoml.bridge.core.data.model.Loadable
import com.begoml.bridge.core.data.model.toClub
import com.begoml.bridge.core.data.sportsdb.SportsDbApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.days

/** The club profile: one row that changes about once a year, so it is held for a week. */
class ClubRepository internal constructor(
    private val teamId: String,
    private val api: SportsDbApi,
    private val dao: ClubDao,
    private val syncer: Syncer,
) {

    fun club(): Flow<Loadable<Club>> = persistedResource(
        stored = dao.observe(teamId).map { entity -> entity?.toClub() },
    ) {
        syncer.sync(key = "club:$teamId", ttl = 7.days) { fetch() }
    }

    suspend fun refresh() {
        syncer.sync(key = "club:$teamId", ttl = null, force = true) { fetch() }
    }

    private suspend fun fetch() {
        val club = api.team(teamId)?.toClub() ?: return
        dao.upsert(club.toEntity())
    }
}
