plugins {
    id("bridge.kmp.compose")
}

kotlin {
    // iOS, the desktop and the browser all draw through Skia, so one runtime-shader actual serves
    // all three; Android has AGSL and keeps its own.
    applyDefaultHierarchyTemplate()

    sourceSets {
        val skikoMain = create("skikoMain") { dependsOn(getByName("commonMain")) }
        getByName("iosMain").dependsOn(skikoMain)
        getByName("desktopMain").dependsOn(skikoMain)
        getByName("wasmJsMain").dependsOn(skikoMain)

        commonMain.dependencies {

            implementation(libs.compose.components.resources)
            implementation(libs.compose.material3)
            implementation(libs.coil.compose)
            implementation(libs.coil.networkKtor)
            implementation(libs.haze)
            implementation(libs.haze.blur)
        }
        getByName("desktopMain").dependencies {
            // A pure-JVM H.264 decoder. It is here instead of JavaFX or VLC because its frames
            // are drawn by Compose like any other bitmap: the panel scrolls and clips with the
            // page, which an interop surface over the window does not.
            implementation(libs.jcodec)
            implementation(libs.jcodec.javase)
        }
        getByName("wasmJsMain").dependencies {
            implementation(libs.kotlinx.browser)
        }
        androidMain.dependencies {
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.ui)
        }
    }
}


