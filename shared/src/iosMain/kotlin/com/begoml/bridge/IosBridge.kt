package com.begoml.bridge

import com.begoml.bridge.core.background.BackgroundRefresh
import com.begoml.bridge.di.startBridge
import com.begoml.bridge.feature.club.ClubComponent
import com.begoml.bridge.feature.matches.detail.MatchDetailComponent
import com.begoml.bridge.feature.matches.matchday.MatchdayComponent
import com.begoml.bridge.feature.matches.season.SeasonComponent
import com.begoml.bridge.feature.player.PlayerComponent
import com.begoml.bridge.feature.squad.SquadComponent
import com.begoml.bridge.navigation.router.AppRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf

/**
 * What Swift calls before it draws anything.
 *
 * The graph used to be started from inside the Compose entry point, which worked only because
 * that entry point was the app. A native UI has several entry points and none of them is the app,
 * so starting is its own call — idempotent, because SwiftUI is free to build a view twice.
 */
object IosBridge {

    private var koin: Koin? = null

    fun start() {
        if (koin != null) return
        koin = startBridge().koin.also { graph ->
            // BGTaskScheduler refuses registrations made after the app has finished launching.
            graph.get<BackgroundRefresh>().schedule()
        }
    }

    /**
     * Where the app has been asked to go.
     *
     * A stream rather than a call, because the asking and the moving belong to different owners:
     * a feature decides, the host that holds the real stack obeys.
     */
    val navigation: Flow<IosNavigation>
        get() = graph().get<AppRouter>().commands.mapNotNull { command -> command.toIosNavigation() }

    fun matchday(): MatchdayComponent = graph().get()

    fun season(): SeasonComponent = graph().get()

    fun squad(): SquadComponent = graph().get()

    fun club(): ClubComponent = graph().get()

    fun player(): PlayerComponent = graph().get()

    fun matchDetail(matchId: String): MatchDetailComponent =
        graph().get { parametersOf(matchId) }

    private fun graph(): Koin = requireNotNull(koin) { "start() first" }
}
