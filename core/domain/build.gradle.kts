plugins {
    id("bridge.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Loadable travels with the contracts, and it is a mechanism rather than a fact about
            // football: how a value arrives, not what the value is.
            api(projects.foundation.resource)
            api(libs.kotlinx.datetime)
        }
    }
}
