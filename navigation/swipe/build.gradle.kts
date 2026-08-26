plugins {
    id("bridge.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
        }
    }
}
