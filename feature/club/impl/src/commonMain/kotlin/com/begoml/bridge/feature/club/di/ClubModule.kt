package com.begoml.bridge.feature.club.di

import com.begoml.bridge.foundation.coroutines.DispatcherProvider
import com.begoml.bridge.feature.club.ClubNavigationEntry
import com.begoml.bridge.feature.club.ClubLabelsSource
import com.begoml.bridge.feature.club.ClubViewModel
import com.begoml.bridge.foundation.strings.LabelsLoader
import com.begoml.bridge.foundation.coroutines.stateHolderScope
import com.begoml.bridge.feature.club.api.ClubRouteCodec
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.RouteCodec
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

fun clubModule() = module {
    single { ClubLabelsSource(get()) } bind LabelsLoader::class
    viewModel {
        val scope = stateHolderScope()
        ClubViewModel(
            scope = scope,
            repository = get(),
            club = get(),
            labels = get<ClubLabelsSource>().labels,
            ioDispatcher = get<DispatcherProvider>().io,
            logger = get(),
            analytics = get(),
        )
    }
    single { ClubNavigationEntry() } bind FeatureNavigationEntry::class
    single { ClubRouteCodec() } bind RouteCodec::class
}
