plugins {
    id("bridge.kmp.library")
}

kotlin {
    // DataStore keeps a preferences file, which every target but the browser has somewhere to put.
    applyDefaultHierarchyTemplate()

    sourceSets {
        val fileMain = create("fileMain") {
            dependsOn(getByName("commonMain"))
            dependencies {
                implementation(libs.androidx.datastore.preferences.core)
            }
        }
        getByName("androidMain").dependsOn(fileMain)
        getByName("iosMain").dependsOn(fileMain)
        getByName("desktopMain").dependsOn(fileMain)

        commonMain.dependencies {
            api(projects.core.features.following.api)
            implementation(projects.foundation.tessera)
            implementation(projects.foundation.coroutines)
            implementation(projects.foundation.logger.api)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        getByName("wasmJsMain").dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}
