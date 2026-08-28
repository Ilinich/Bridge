package com.begoml.bridge.foundation.strings

/**
 * A feature's fixed words, read once before anything is drawn.
 *
 * Contributed to the graph the way routes and navigation entries are: the host asks for every
 * loader it can find and waits for them, and no module has to know which features exist. A feature
 * that gains a screen registers one more of these and nothing above it changes.
 *
 * The words are read before any ViewModel is built, which is what lets a ui state hold them
 * without a null.
 */
interface LabelsLoader {

    suspend fun load()
}
