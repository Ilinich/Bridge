plugins {
    id("bridge.kmp.library")
}

kotlin {
    sourceSets {
        getByName("wasmJsMain").dependencies {
            implementation(libs.kotlinx.browser)
        }

        commonMain.dependencies {
            api(projects.core.connectivity.api)
            implementation(projects.foundation.tessera)
            implementation(projects.foundation.coroutines)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
