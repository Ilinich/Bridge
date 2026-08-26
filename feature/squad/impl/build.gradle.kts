plugins {
    id("bridge.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.squad.api)

            implementation(projects.core.data)
            implementation(projects.navigation.core)
            implementation(projects.uikit)

            implementation(libs.compose.components.resources)
            implementation(libs.compose.material3)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
    }
}
