plugins {
    id("bridge.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.following.api)
            implementation(projects.foundation.tessera)
            implementation(projects.foundation.logger.api)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
