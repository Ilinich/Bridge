package com.begoml.bridge.feature.club.di

import com.begoml.bridge.core.data.di.IoDispatcher
import com.begoml.bridge.feature.club.ClubNavigationEntry
import com.begoml.bridge.feature.club.ClubViewModel
import com.begoml.bridge.feature.club.api.ClubRouteCodec
import com.begoml.bridge.navigation.FeatureNavigationEntry
import com.begoml.bridge.navigation.RouteCodec
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

fun clubModule() = module {
    viewModel { ClubViewModel(repository = get(), ioDispatcher = get(IoDispatcher)) }
    single { ClubNavigationEntry() } bind FeatureNavigationEntry::class
    single { ClubRouteCodec() } bind RouteCodec::class
}
