plugins {
    id("bridge.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.navigation.core)
        }
    }
}
