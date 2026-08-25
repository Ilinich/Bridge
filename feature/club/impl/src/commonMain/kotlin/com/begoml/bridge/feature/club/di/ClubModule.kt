package com.begoml.bridge.feature.club.di

import com.begoml.bridge.feature.club.ClubDelegate
import com.begoml.bridge.feature.club.ClubNavigationEntry
import com.begoml.bridge.feature.club.api.ClubRouteCodec
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.RouteCodec
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.bind
import org.koin.dsl.module

fun clubModule() = module {
    factory { (scope: CoroutineScope) -> ClubDelegate(scope = scope, repository = get()) }
    single { ClubNavigationEntry() } bind FeatureNavigationEntry::class
    single { ClubRouteCodec() } bind RouteCodec::class
}
