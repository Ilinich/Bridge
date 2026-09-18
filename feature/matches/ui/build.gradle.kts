plugins {
    id("bridge.android.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.matches.api)
            implementation(projects.feature.matches.impl)
            implementation(projects.feature.club.api)
            implementation(projects.feature.player.api)
            implementation(projects.core.domain)
            implementation(projects.foundation.tessera)
            implementation(projects.navigation.core)
            implementation(projects.uikit)
            implementation(projects.core.analytics.api)
            implementation(projects.core.connectivity.api)
            implementation(projects.core.features.following.api)

            implementation(libs.moko.resources)
            implementation(libs.moko.resources.compose)
        }
    }
}
