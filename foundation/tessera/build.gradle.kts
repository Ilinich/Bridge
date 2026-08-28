plugins {
    id("bridge.kmp.compose")
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            // The ViewModel constructor this app hands its scope to is the thing under test.
            implementation(libs.androidx.lifecycle.viewmodelCompose)
        }
    }
}
