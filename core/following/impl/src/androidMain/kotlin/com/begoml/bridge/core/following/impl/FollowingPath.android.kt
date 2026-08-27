package com.begoml.bridge.core.following.impl

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module

internal actual fun Module.bindFollowingPath() {
    single { FollowingPath(androidContext().filesDir.resolve(FollowingFileName).absolutePath) }
}
