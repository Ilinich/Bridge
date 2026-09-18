plugins {
    id("bridge.kmp.library")
    alias(libs.plugins.mokoResources)
}

multiplatformResources {
    resourcesPackage.set("com.begoml.bridge.feature.matches")
    resourcesClassName.set("MatchesStrings")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.matches.api)

            // Another feature's destinations, and nothing else of it.
            implementation(projects.feature.club.api)
            implementation(projects.feature.player.api)

            implementation(projects.core.analytics.api)
            implementation(projects.core.connectivity.api)
            implementation(projects.core.features.following.api)
            implementation(projects.foundation.tessera)
            implementation(projects.foundation.coroutines)
            implementation(projects.core.domain)
            implementation(projects.foundation.format)

            api(libs.moko.resources)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.koin.core)
        }
    }
}
