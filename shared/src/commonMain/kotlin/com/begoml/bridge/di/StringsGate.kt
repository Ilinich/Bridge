package com.begoml.bridge.di

import com.begoml.bridge.foundation.coroutines.AppScope
import com.begoml.bridge.foundation.strings.LabelsLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Whether the app's fixed words have been read yet.
 *
 * The host waits on this rather than starting the work itself: a ViewModel takes its words by
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
 * Asks every feature that has words to read them, once, before anything is drawn.
 *
 * It asks the graph rather than a list of features: a feature contributes a [LabelsLoader] the way
 * it contributes a route, so this function does not change when a screen is added. Where the words
 * come from and which thread reads them is the resolver's business, not this one's.
 */
fun loadStrings(koin: Koin) {
    koin.get<AppScope>().launch {
        koin.getAll<LabelsLoader>().forEach { loader -> loader.load() }
        koin.get<StringsGate>().markReady()
    }
}
