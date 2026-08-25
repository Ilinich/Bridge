package com.begoml.bridge.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubDao {

    @Query("SELECT * FROM club WHERE id = :id")
    fun observe(id: String): Flow<ClubEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(club: ClubEntity)
}

@Dao
interface VenueDao {

    @Query("SELECT * FROM venue WHERE id = :id")
    fun observe(id: String): Flow<VenueEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(venue: VenueEntity)
}

@Dao
interface PlayerDao {

    @Query("SELECT * FROM player ORDER BY ordinal")
    fun observeAll(): Flow<List<PlayerEntity>>

    @Query("DELETE FROM player")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<PlayerEntity>)

    /** A squad is replaced wholesale: a transferred player must disappear, not linger. */
    @Transaction
    suspend fun replaceAll(players: List<PlayerEntity>) {
        deleteAll()
        insertAll(players)
    }
}

@Dao
interface SeasonDao {

    @Query("SELECT * FROM season_match WHERE season = :season ORDER BY round, kickoffMillis")
    fun observeSeason(season: String): Flow<List<SeasonMatchEntity>>

    @Query("SELECT * FROM season_match WHERE id = :id")
    fun observeMatch(id: String): Flow<SeasonMatchEntity?>

    @Query("SELECT COUNT(*) FROM season_match WHERE season = :season")
    suspend fun count(season: String): Int

    @Query("DELETE FROM season_match WHERE season = :season")
    suspend fun deleteSeason(season: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(matches: List<SeasonMatchEntity>)

    @Transaction
    suspend fun replaceSeason(season: String, matches: List<SeasonMatchEntity>) {
        deleteSeason(season)
        insertAll(matches)
    }
}

@Dao
interface FreshnessDao {

    @Query("SELECT fetchedAtMillis FROM freshness WHERE key = :key")
    suspend fun fetchedAt(key: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun stamp(entry: FreshnessEntity)
}
