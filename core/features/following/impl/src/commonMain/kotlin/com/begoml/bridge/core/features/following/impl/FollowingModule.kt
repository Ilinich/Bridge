package com.begoml.bridge.core.features.following.impl

import com.begoml.bridge.core.features.following.FollowingFeature
import com.begoml.bridge.foundation.coroutines.stateHolderScope
import org.koin.core.module.Module
import org.koin.dsl.module

/** Where the platform keeps a handful of ids somebody chose. */
internal expect fun Module.bindFollowingStore()

fun followingModule(): Module = module {
    bindFollowingStore()
    single<FollowingFeature> {
        FollowingFeatureImpl(
            scope = stateHolderScope(),
            store = get(),
            plugins = listOf(FollowingLogPlugin(get())),
        )
    }
}
