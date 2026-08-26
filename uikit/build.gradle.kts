plugins {
    id("bridge.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.foundation.tessera)

            implementation(libs.compose.components.resources)
            implementation(libs.compose.material3)
            implementation(libs.coil.compose)
            implementation(libs.coil.networkKtor)
            implementation(libs.haze)
            implementation(libs.haze.blur)
        }
        androidMain.dependencies {
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.ui)
        }
    }
}


