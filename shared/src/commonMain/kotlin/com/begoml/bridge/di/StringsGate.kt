package com.begoml.bridge.di

import com.begoml.bridge.feature.club.loadClubLabels
import com.begoml.bridge.feature.matches.detail.loadMatchDetailLabels
import com.begoml.bridge.feature.matches.matchday.loadMatchdayLabels
import com.begoml.bridge.feature.player.loadPlayerLabels
import com.begoml.bridge.foundation.coroutines.AppScope
import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Whether the app's fixed words are in the graph yet.
 *
 * The host waits on this rather than starting the work itself: a ViewModel takes its labels by
 * constructor, so a screen composed before the read finished would have nothing to take.
 */
class StringsGate internal constructor() {

    private val loaded = MutableStateFlow(false)

    val ready: StateFlow<Boolean> = loaded.asStateFlow()

    internal fun markReady() {
        loaded.value = true
    }
}

fun stringsModule(): Module = module { single { StringsGate() } }

/**
 * Reads every fixed word in the app once, off the main thread, before any screen exists.
 *
 * The words do not change while the app is open, and reading them is a bundle lookup on both
 * platforms — work that has no business on the thread that draws. Reading them per screen made
 * every ui state carry a nullable set of labels and every composable guard against it; reading
 * them here makes them a value in the graph.
 *
 * Only fixed words. Anything shaped around data — a round number, a scoreline — stays where the
 * data is, because it has to be formatted per row.
 */
fun loadStrings(koin: Koin) {
    val dispatchers: DispatcherProvider = koin.get()
    koin.get<AppScope>().launch {
        val club = withContext(dispatchers.io) { loadClubLabels() }
        val matchday = withContext(dispatchers.io) { loadMatchdayLabels() }
        val matchDetail = withContext(dispatchers.io) { loadMatchDetailLabels() }
        val player = withContext(dispatchers.io) { loadPlayerLabels() }

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
        koin.get<StringsGate>().markReady()
    }
}
