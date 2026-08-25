package com.begoml.bridge.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "club")
class ClubEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val foundedYear: Int?,
    val stadium: String?,
    val stadiumCapacity: Int?,
    val location: String?,
    val description: String?,
    val badgeUrl: String?,
    /** Newline-joined; a handful of URLs does not justify a second table. */
    val fanartUrls: String,
)

@Entity(tableName = "player")
class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val position: String?,
    val shirtNumber: String?,
    val nationality: String?,
    val height: String?,
    val description: String?,
    val cutoutUrl: String?,
    val thumbUrl: String?,
    /** Preserves the order the feed returned, which is the order the grid shows. */
    val ordinal: Int,
)

@Entity(tableName = "season_match")
class SeasonMatchEntity(
    @PrimaryKey val id: String,
    val season: String,
    val round: Int,
    val kickoffMillis: Long,
    val homeName: String,
    val homeCode: String,
    val awayName: String,
    val awayCode: String,
    val homeGoals: Int?,
    val awayGoals: Int?,
)

/**
 * When each resource was last fetched.
 *
 * On disk rather than in memory on purpose: after a process death an in-memory stamp would be gone
 * and every cold start would re-fetch, which is exactly what persisting the data was meant to avoid.
 */
@Entity(tableName = "freshness")
class FreshnessEntity(
    @PrimaryKey val key: String,
    val fetchedAtMillis: Long,
)
