plugins {
    id("bridge.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // The launcher reports what escapes, so it needs somewhere to report to.
            api(projects.foundation.logger.api)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
