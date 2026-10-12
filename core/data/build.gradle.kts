plugins {
    id("bridge.kmp.library")
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

room {
    // Schemas are checked in: a released version is never regenerated, only added to.
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    listOf(
        "kspAndroid",
        "kspIosArm64",
        "kspIosSimulatorArm64",
        "kspDesktop",
    ).forEach { add(it, libs.room.compiler) }
}

kotlin {
    // Room covers every target but the browser, which has no SQLite to talk to and stores the
    // same data in IndexedDB instead. The shared half of the module never sees either.
    applyDefaultHierarchyTemplate()

    sourceSets {
        val roomMain = create("roomMain") {
            dependsOn(getByName("commonMain"))
            dependencies {
                implementation(libs.room.runtime)
                implementation(libs.sqlite.bundled)
            }
        }
        getByName("androidMain").dependsOn(roomMain)
        getByName("iosMain").dependsOn(roomMain)
        getByName("desktopMain").dependsOn(roomMain)

        commonMain.dependencies {
            api(projects.core.domain)
            implementation(projects.foundation.resource)
            api(projects.foundation.coroutines)
            implementation(projects.foundation.strings)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        getByName("desktopMain").dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        getByName("wasmJsMain").dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}
