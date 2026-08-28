package com.begoml.bridge.di

import com.begoml.bridge.feature.club.loadClubLabels
import com.begoml.bridge.feature.matches.detail.loadMatchDetailLabels
import com.begoml.bridge.feature.matches.matchday.loadMatchdayLabels
import com.begoml.bridge.feature.player.loadPlayerLabels
import org.koin.core.Koin
import org.koin.dsl.module

/**
 * Reads every fixed word in the app once, before any screen exists.
 *
 * The words do not change while the app is open, and resolving them is suspending on both
 * platforms — the strings live in a bundle, not in a constant. Reading them per screen made every
 * ui state carry a nullable set of labels and every composable guard against it; reading them here
 * makes them a value in the graph, so a screen either has them or has not been built yet.
 *
 * Only fixed words. Anything shaped around data — a round number, a scoreline — stays where the
 * data is, because it has to be formatted per row.
 */
suspend fun loadStrings(koin: Koin) {
    val club = loadClubLabels()
    val matchday = loadMatchdayLabels()
    val matchDetail = loadMatchDetailLabels()
    val player = loadPlayerLabels()

    koin.loadModules(
        listOf(
            module {
                single { club }
                single { matchday }
                single { matchDetail }
                single { player }
            },
        ),
    )
}
