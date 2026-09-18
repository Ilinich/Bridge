plugins {
    id("bridge.kmp.compose.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.navigation.routes)
            api(projects.navigation.swipe)

            api(libs.navigation3.ui)
            api(libs.lifecycle.viewmodel.navigation3)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
