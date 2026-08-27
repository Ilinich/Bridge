package com.begoml.bridge.feature.club.di

import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import com.begoml.bridge.feature.club.ClubNavigationEntry
import com.begoml.bridge.feature.club.ClubDelegate
import com.begoml.bridge.feature.club.ClubViewModel
import com.begoml.bridge.foundation.coroutines.stateHolderScope
import com.begoml.bridge.feature.club.api.ClubRouteCodec
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.RouteCodec
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

fun clubModule() = module {
    viewModel {
        val scope = stateHolderScope()
        ClubViewModel(
            scope = scope,
            delegate = ClubDelegate(scope = scope, repository = get(), club = get()),
            ioDispatcher = get<DispatcherProvider>().io,
            analytics = get(),
        )
    }
    single { ClubNavigationEntry() } bind FeatureNavigationEntry::class
    single { ClubRouteCodec() } bind RouteCodec::class
}
