package com.begoml.bridge.feature.matches.di

import com.begoml.bridge.feature.matches.matchday.MatchdayFeature
import com.begoml.bridge.feature.matches.season.SeasonFeature
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module
import kotlin.time.Clock

fun matchesModule() = module {
    factory { (scope: CoroutineScope) ->
        MatchdayFeature(
            scope = scope,
            clubRepository = get(),
            matchRepository = get(),
            nowMillis = { Clock.System.now().toEpochMilliseconds() },
        )
    }
    factory { (scope: CoroutineScope) ->
        SeasonFeature(
            scope = scope,
            matchRepository = get(),
            nowMillis = { Clock.System.now().toEpochMilliseconds() },
        )
    }
}
