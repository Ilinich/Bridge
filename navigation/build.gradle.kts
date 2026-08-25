plugins {
    id("bridge.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.uikit)

            api(libs.navigation3.ui)
            implementation(libs.lifecycle.viewmodel.navigation3)
            implementation(libs.koin.core)
        }
    }
}
