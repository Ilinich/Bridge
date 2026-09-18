plugins {
    id("bridge.kmp.compose.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
