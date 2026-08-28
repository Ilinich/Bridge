plugins {
    // AGP is already on the classpath through the included build, so it carries no version here.
    // AGP 9 brings its own Kotlin support; the separate Kotlin plugin is refused.
    id("com.android.test")
    alias(libs.plugins.baselineProfile)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.begoml.bridge.benchmark"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 28
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Macrobenchmark refuses to measure on an emulator, and it is right: a virtualised CPU and
        // a host GPU do not predict a phone, and an improvement seen here can be a regression
        // there. The refusal is left in place by default. Passing
        // -Pbridge.benchmark.allowEmulator=true turns it into a warning, which is worth doing only
        // to prove the pipeline runs -- the numbers it then prints are relative, not device truth.
        if (providers.gradleProperty("bridge.benchmark.allowEmulator").orNull == "true") {
            testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    targetProjectPath = ":androidApp"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    // A managed device rather than whatever happens to be plugged in. useConnectedDevices picks up
    // every attached device, including someone's personal phone, and installs and drives the
    // benchmark on it. This also makes a run reproducible and available to CI.
    testOptions {
        managedDevices {
            localDevices {
                create("benchmarkDevice") {
                    device = "Pixel 6"
                    apiLevel = 34
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

baselineProfile {
    useConnectedDevices = false
    managedDevices += "benchmarkDevice"
}

// The generator and the measurements live in the variants that can actually run them: generating a
// profile must not also fire the benchmarks, and a benchmark that Requires a profile cannot run in
// the variant that is being used to produce one. Shared journey code stays in main.

dependencies {
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.testExt.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.junit)
}

detekt {
    parallel = true
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    basePath = rootProject.projectDir.absolutePath
}

dependencies {
    detektPlugins(project(":detekt-rules"))
}

tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt over every Kotlin source set of this module."
    dependsOn(tasks.withType<io.gitlab.arturbosch.detekt.Detekt>())
}
