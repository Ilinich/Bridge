plugins {
    id("bridge.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.squad.api)

            // Another feature's destination, and nothing else of it.
            implementation(projects.feature.player.api)

            implementation(projects.core.analytics.api)
            implementation(projects.core.features.following.api)
            implementation(projects.foundation.tessera)
            implementation(projects.foundation.coroutines)
            implementation(projects.core.domain)

            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.koin.core)
        }
    }
}
