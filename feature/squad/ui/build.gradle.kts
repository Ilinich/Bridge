plugins {
    id("bridge.android.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.squad.api)
            implementation(projects.feature.squad.impl)
            implementation(projects.feature.player.api)
            implementation(projects.core.domain)
            implementation(projects.foundation.tessera)
            implementation(projects.navigation.core)
            implementation(projects.uikit)
            implementation(projects.core.analytics.api)

            implementation(libs.moko.resources)
            implementation(libs.moko.resources.compose)
        }
    }
}
