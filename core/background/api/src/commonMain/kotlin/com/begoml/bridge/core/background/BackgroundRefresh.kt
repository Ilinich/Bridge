package com.begoml.bridge.core.background

/**
 * Asks the platform to run [RefreshWork] roughly once a day.
 *
 * Roughly is the contract, and it differs by platform rather than by implementation quality:
 * Android will run the work on a real period, while iOS treats a submitted task as permission to
 * run rather than a promise, and may run it later or not at all. Nothing may depend on it having
 * happened; it is an optimisation over the next foreground launch, never a substitute for one.
 */
interface BackgroundRefresh {

    fun schedule()
}

/**
 * The unit of work the scheduler runs.
 *
 * Declared here so the scheduler stays free of anything it refreshes: this module knows how to ask
 * a platform for background time and nothing about clubs, squads or seasons.
 */
fun interface RefreshWork {

    /** Returns whether the refresh completed, which the platform reports back as success. */
    suspend fun run(): Boolean
}
