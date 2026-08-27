package com.begoml.bridge.core.domain

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private const val SeasonStartsInMonthOrdinal = 7
private const val CenturyDivisor = 100
private const val TwoDigits = 2

/**
 * The season a date belongs to, in the form the fixture feed uses.
 *
 * English football seasons run August to May, so anything from August onwards belongs to the
 * season that opens in that calendar year. Deriving it means the app does not go stale every
 * August the way a hardcoded id does.
 */
fun seasonIdAt(nowMillis: Long): String {
    val date = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(TimeZone.UTC).date
    val startYear = if (date.month.ordinal >= SeasonStartsInMonthOrdinal) date.year else date.year - 1
    return seasonId(startYear)
}

fun previousSeasonId(seasonId: String): String =
    seasonId(seasonId.substringBefore('-').toInt() - 1)

private fun seasonId(startYear: Int): String {
    val endShort = ((startYear + 1) % CenturyDivisor).toString().padStart(TwoDigits, '0')
    return "$startYear-$endShort"
}
