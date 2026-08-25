package com.begoml.bridge.navigation.di

import com.begoml.bridge.navigation.Navigator
import com.begoml.bridge.navigation.NavigatorHolder
import org.koin.dsl.bind
import org.koin.dsl.module

fun navigationModule() = module {
    single { NavigatorHolder() } bind Navigator::class
}
