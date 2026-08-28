package com.begoml.bridge.navigation.di

import com.begoml.bridge.navigation.router.AppRouter
import com.begoml.bridge.navigation.router.AppRouterImpl
import com.begoml.bridge.navigation.router.NavigationHost
import org.koin.dsl.binds
import org.koin.dsl.module

fun navigationModule() = module {
    single { AppRouterImpl() } binds arrayOf(AppRouter::class, NavigationHost::class)
}
