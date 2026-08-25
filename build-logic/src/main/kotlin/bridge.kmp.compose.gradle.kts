plugins {
    id("bridge.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("compose-runtime").get())
            implementation(libs.findLibrary("compose-foundation").get())
            implementation(libs.findLibrary("compose-ui").get())
            implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
            implementation(libs.findLibrary("kotlinx-collections-immutable").get())
        }
    }
}
