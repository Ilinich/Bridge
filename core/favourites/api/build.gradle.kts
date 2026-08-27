plugins {
    id("bridge.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.foundation.tessera)
            api(libs.kotlinx.collections.immutable)
        }
    }
}
