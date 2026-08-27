plugins {
    id("bridge.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.club.api)

            implementation(projects.core.analytics.api)
            implementation(projects.core.connectivity.api)
            implementation(projects.foundation.tessera)
            implementation(projects.core.data)
            implementation(projects.navigation.core)
            implementation(projects.uikit)

            implementation(libs.compose.components.resources)
            implementation(libs.compose.material3)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewModel)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
    }
}
