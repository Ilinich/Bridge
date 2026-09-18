plugins {
    id("bridge.android.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.player.api)
            implementation(projects.feature.player.impl)
            implementation(projects.core.domain)
            implementation(projects.foundation.tessera)
            implementation(projects.navigation.core)
            implementation(projects.uikit)
            implementation(projects.core.features.following.api)

            implementation(libs.moko.resources)
            implementation(libs.moko.resources.compose)
        }
    }
}
