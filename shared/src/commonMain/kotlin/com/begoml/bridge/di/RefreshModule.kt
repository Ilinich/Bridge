package com.begoml.bridge.di

import com.begoml.bridge.core.domain.model.FollowedClub
import com.begoml.bridge.core.domain.repository.ClubRepository
import com.begoml.bridge.core.domain.repository.MatchRepository
import com.begoml.bridge.core.domain.repository.SquadRepository
import com.begoml.bridge.core.background.RefreshWork
import com.begoml.bridge.foundation.logger.Logger
import com.begoml.bridge.foundation.logger.warn
import kotlinx.coroutines.CancellationException
import org.koin.dsl.module

private const val RefreshTag = "Refresh"

/**
 * What the daily background run actually does.
 *
 * It lives here rather than in the scheduler module because the scheduler must not know what a
 * club or a season is; it asks the platform for time and runs whatever this binds.
 */
fun refreshModule() = module {
    single<RefreshWork> {
        val club: ClubRepository = get()
        val squad: SquadRepository = get()
        val matches: MatchRepository = get()
        val logger: Logger = get()
        val followed: FollowedClub = get()

        RefreshWork {
            // One failure must not cancel the others: a squad that could not be fetched is no
            // reason to leave the calendar stale as well. The run reports success only if every
            // part succeeded, so the platform can retry.
            // Cancellation is rethrown rather than counted as a failure: the platform revokes
            // the window by cancelling, and a swallowed cancellation would let the run carry on
            // making requests after its time is up and then report a result nobody asked for.
            val refreshes = listOf<suspend () -> Unit>(
                { club.refresh(followed.id) },
                { squad.refresh(followed.id) },
                { matches.refresh() },
            )
            val results = refreshes.map { refresh ->
                runCatching { refresh() }
                    .onFailure { error -> if (error is CancellationException) throw error }
            }
            results.forEach { result ->
                result.exceptionOrNull()?.let { error ->
                    logger.warn(RefreshTag, "a source failed to refresh", error)
                }
            }
            results.all { it.isSuccess }
        }
    }
}
