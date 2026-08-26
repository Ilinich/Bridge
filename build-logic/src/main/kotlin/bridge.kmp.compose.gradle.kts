plugins {
    id("bridge.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    // A manual dependsOn edge (below) switches the default hierarchy off, which would strip
    // iosMain of its actuals. Re-applying it keeps both.
    applyDefaultHierarchyTemplate()

    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.findLibrary("compose-uiTest").get())
        }
        // The Android device compilation is not part of the default test hierarchy, so the shared
        // UI tests would silently compile nowhere and the connected run would report success on an
        // empty suite. This is what puts them on a device.
        named("androidDeviceTest") { dependsOn(commonTest.get()) }
        commonMain.dependencies {
            implementation(libs.findLibrary("compose-runtime").get())
            implementation(libs.findLibrary("compose-foundation").get())
            implementation(libs.findLibrary("compose-ui").get())
            implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
            implementation(libs.findLibrary("kotlinx-collections-immutable").get())
        }
    }
}

dependencies {
    // Espresso 3.5, which the Compose test artifact still resolves, calls InputManager.getInstance;
    // newer platforms removed it and every device test dies on the first assertion.
    "androidDeviceTestImplementation"(libs.findLibrary("androidx-espresso-core").get())
    "androidDeviceTestImplementation"(libs.findLibrary("androidx-test-runner").get())
    "androidDeviceTestImplementation"(libs.findLibrary("compose-uiTestManifest").get())
}

// Compose UI tests need a real UI toolkit, and the JVM host run has none: the same file compiles
// there and then dies on an android.jar stub. They run natively on iosSimulatorArm64Test and on a
// device via connectedAndroidDeviceTest; the host run skips them by name.
tasks.withType<Test>().configureEach {
    exclude("**/*UiTest*")
    // A module whose only tests are UI tests legitimately runs nothing here.
    failOnNoDiscoveredTests = false
}
