package com.begoml.bridge.di

import com.begoml.bridge.core.data.di.dataModules
import com.begoml.bridge.core.analytics.impl.analyticsModule
import com.begoml.bridge.core.background.impl.backgroundModule
import com.begoml.bridge.core.connectivity.impl.connectivityModule
import com.begoml.bridge.core.following.impl.followingModule
import com.begoml.bridge.foundation.logger.impl.loggerModule
import com.begoml.bridge.feature.club.di.clubModule
import com.begoml.bridge.navigation.di.navigationModule
import com.begoml.bridge.feature.matches.di.matchesModule
import com.begoml.bridge.feature.player.di.playerModule
import com.begoml.bridge.feature.squad.di.squadModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Starts the graph.
 *
 * Both platforms call this, and iOS calls it from its own entry point rather than inheriting one,
 * which is why it lives here instead of inside the Android application class.
 */
fun startBridge(
    isLoggingEnabled: Boolean = true,
    declaration: KoinAppDeclaration = {},
): KoinApplication = startKoin {
    declaration()
    modules(
        listOf(
            loggerModule(isLoggingEnabled),
            analyticsModule(),
            connectivityModule(),
            followingModule(),
            backgroundModule(),
            refreshModule(),
        ) +
            dataModules() + navigationModule() +
            matchesModule() + squadModule() + playerModule() + clubModule(),
    )
}
