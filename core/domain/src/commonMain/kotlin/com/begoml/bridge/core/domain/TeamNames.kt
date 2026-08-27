package com.begoml.bridge.core.domain

/**
 * Reconciles club names between the two sources and supplies a three-letter code.
 *
 * openfootball writes "Chelsea FC" where TheSportsDB writes "Chelsea", so the same club would
 * otherwise be two clubs. The code doubles as the monogram the UI draws: no full badge map exists
 * on the free tier, and club artwork is deliberately absent from this repository.
 *
 * An unknown name is **degraded, never dropped** — a fixture must not vanish from the calendar
 * because a spelling is unfamiliar.
 */
object TeamNames {

    private const val CodeLength = 3

    private val codesByName: Map<String, String> = mapOf(
        "arsenal" to "ARS",
        "aston villa" to "AVL",
        "bournemouth" to "BOU",
        "brentford" to "BRE",
        "brighton and hove albion" to "BHA",
        "brighton hove albion" to "BHA",
        "burnley" to "BUR",
        "chelsea" to "CHE",
        "crystal palace" to "CRY",
        "everton" to "EVE",
        "fulham" to "FUL",
        "leeds united" to "LEE",
        "liverpool" to "LIV",
        "luton town" to "LUT",
        "manchester city" to "MCI",
        "manchester united" to "MUN",
        "newcastle united" to "NEW",
        "nottingham forest" to "NFO",
        "sheffield united" to "SHU",
        "sunderland" to "SUN",
        "tottenham hotspur" to "TOT",
        "west ham united" to "WHU",
        "wolverhampton wanderers" to "WOL",
    )

    private val suffixes = listOf(" fc", " afc", " f.c.", " a.f.c.")

    /** "Chelsea FC" and "Chelsea" both normalise to the same key. */
    fun normalise(name: String): String {
        var normalised = name.trim().lowercase()
        suffixes.forEach { suffix ->
            if (normalised.endsWith(suffix)) normalised = normalised.removeSuffix(suffix)
            if (normalised.startsWith(suffix.trim() + " ")) {
                normalised = normalised.removePrefix(suffix.trim() + " ")
            }
        }
        return normalised.trim()
    }

    fun displayName(name: String): String = name.trim().let { raw ->
        suffixes.fold(raw) { acc, suffix ->
            if (acc.lowercase().endsWith(suffix)) acc.dropLast(suffix.length).trim() else acc
        }
    }

    fun code(name: String): String =
        codesByName[normalise(name)] ?: fallbackCode(name)

    private fun fallbackCode(name: String): String {
        val letters = displayName(name).filter { it.isLetter() }
        return letters.take(CodeLength).uppercase().ifEmpty { "???" }
    }

    fun matches(left: String, right: String): Boolean = normalise(left) == normalise(right)
}
