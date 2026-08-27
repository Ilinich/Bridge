package com.begoml.bridge.core.domain.repository

import com.begoml.bridge.core.domain.model.Loadable
import com.begoml.bridge.core.domain.model.Match
import com.begoml.bridge.core.domain.model.Season
import com.begoml.bridge.core.domain.model.SeasonMatch
import kotlinx.coroutines.flow.Flow

/**
 * Fixtures, results and the season calendar.
 *
 * The two ends of a club's schedule come from one feed and the whole calendar from another, which
 * is why they have different lifetimes here: a fixture list is short and volatile and is cached in
 * memory, while a season is 380 rows and lives on disk.
 */
interface MatchRepository {

    /**
     * The club's next fixture, or `null` inside [Loadable.Content] when there is none announced.
     *
     * Null is an answer, not an absence of one: between seasons the feed genuinely has nothing to
     * report, and a screen says so rather than waiting forever.
     *
     * @param teamId the club's id in the upstream feed, never a name.
     */
    fun nextMatch(teamId: String): Flow<Loadable<Match?>>

    /** The club's most recent finished match, on the same terms as [nextMatch]. */
    fun lastResult(teamId: String): Flow<Loadable<Match?>>

    /**
     * The league calendar of the season now being played.
     *
     * Not a club's calendar — every fixture of the competition, which is what lets the screen page
     * through rounds without asking again. Falls back to the previous season while the new one is
     * unpublished, so the screen is never empty in the summer.
     */
    fun season(): Flow<Loadable<Season>>

    /**
     * One fixture out of the stored calendar, or `null` inside [Loadable.Content] when the
     * calendar has no such id.
     *
     * Reads what [season] has already stored and never fetches on its own: an id can only have
     * come from a calendar that was loaded.
     */
    fun match(id: String): Flow<Loadable<SeasonMatch?>>

    /**
     * Revalidates the calendar and drops the fixture caches, however fresh they are.
     *
     * Switches to the io dispatcher itself, whatever the caller's context.
     */
    suspend fun refresh()
}
