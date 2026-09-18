plugins {
    id("bridge.android.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(projects.navigation.core)
            implementation(projects.navigation.routes)
            implementation(projects.uikit)
            implementation(projects.core.analytics.api)
            implementation(projects.core.connectivity.api)
            implementation(projects.foundation.tessera)
            implementation(projects.foundation.logger.api)
            implementation(projects.feature.club.api)
            implementation(projects.feature.matches.api)
            implementation(projects.feature.squad.api)
            implementation(projects.feature.player.api)
            implementation(projects.feature.club.ui)
            implementation(projects.feature.matches.ui)
            implementation(projects.feature.player.ui)
            implementation(projects.feature.squad.ui)

            implementation(libs.moko.resources)
            implementation(libs.moko.resources.compose)
            implementation(libs.coil.compose)
            implementation(libs.coil.networkKtor)
            implementation(libs.ktor.client.core)
        }
    }
}
