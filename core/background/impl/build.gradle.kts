plugins {
    id("bridge.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.background.api)
            implementation(projects.foundation.logger.api)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.work.runtime)
            implementation(libs.koin.android)
        }
    }
}
