plugins {
    id("bridge.android.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.club.api)
            implementation(projects.feature.club.impl)
            implementation(projects.core.domain)
            implementation(projects.foundation.tessera)
            implementation(projects.navigation.core)
            implementation(projects.uikit)
            implementation(projects.core.analytics.api)
            implementation(projects.core.connectivity.api)

            implementation(libs.moko.resources)
            implementation(libs.moko.resources.compose)
        }
    }
}
