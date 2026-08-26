plugins {
    id("bridge.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.foundation.logger.api)
            implementation(libs.koin.core)
        }
    }
}
