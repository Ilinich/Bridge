package com.begoml.bridge.core.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [
        ClubEntity::class,
        VenueEntity::class,
        PlayerEntity::class,
        SeasonMatchEntity::class,
        FreshnessEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(BridgeDatabaseConstructor::class)
abstract class BridgeDatabase : RoomDatabase() {

    abstract fun clubDao(): ClubDao

    abstract fun venueDao(): VenueDao

    abstract fun playerDao(): PlayerDao

    abstract fun seasonDao(): SeasonDao

    abstract fun freshnessDao(): FreshnessDao
}

@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA")
expect object BridgeDatabaseConstructor : RoomDatabaseConstructor<BridgeDatabase> {
    override fun initialize(): BridgeDatabase
}
