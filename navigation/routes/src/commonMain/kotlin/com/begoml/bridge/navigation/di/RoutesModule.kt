package com.begoml.bridge.navigation.di

import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.AppRouterImpl
import org.koin.dsl.module

fun navigationModule() = module {
    single<AppRouter> { AppRouterImpl() }
}
