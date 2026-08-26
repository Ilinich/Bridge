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
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    targetProjectPath = ":androidApp"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    useConnectedDevices = true
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
