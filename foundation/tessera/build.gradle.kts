plugins {
    id("bridge.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // The state holder itself: a ViewModel and a scope, no toolkit.
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.collections.immutable)
        }
        // Reading a holder from a composition is the Compose host's business, so the binding —
        // and the dependency it needs — stay on the side that has one.
        androidMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            // The ViewModel constructor this app hands its scope to is the thing under test.
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.collections.immutable)
        }
    }
}
