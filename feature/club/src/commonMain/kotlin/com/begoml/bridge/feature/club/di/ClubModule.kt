package com.begoml.bridge.feature.club.di

import com.begoml.bridge.feature.club.ClubDelegate
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module

fun clubModule() = module {
    factory { (scope: CoroutineScope) -> ClubDelegate(scope = scope, repository = get()) }
}
