plugins {
    id("bridge.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.analytics.api)

            // The logger's contract, not its implementation: analytics records events, it does not
            // decide where a line ends up.
            implementation(projects.foundation.logger.api)
            implementation(libs.koin.core)
        }
    }
}
