package com.begoml.bridge.feature.club.di

import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import com.begoml.bridge.feature.club.ClubComponent
import com.begoml.bridge.feature.club.ClubViewModel
import com.begoml.bridge.foundation.coroutines.stateHolderScope
import com.begoml.bridge.feature.club.api.ClubRouteCodec
import com.begoml.bridge.navigation.RouteCodec
import org.koin.dsl.bind
import org.koin.dsl.module

fun clubModule() = module {
    factory {
        val scope = stateHolderScope()
        ClubComponent(
            scope = scope,
            viewModel = ClubViewModel(
            scope = scope,
            repository = get(),
            club = get(),
            ioDispatcher = get<DispatcherProvider>().io,
            logger = get(),
                analytics = get(),
            ),
        )
    }
    single { ClubRouteCodec() } bind RouteCodec::class
}
