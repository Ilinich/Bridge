import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.gitlab.arturbosch.detekt")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(21)

    compilerOptions {
        // expect/actual classes are Beta but supported; the platform shader runtimes need them.
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    iosArm64()
    iosSimulatorArm64()

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            // The shared tests do not run in a browser, and the reason is packaging rather than
            // principle: a module that reaches Compose resources drags skiko's JS glue into its
            // Karma bundle, which the test bundle has no copy of, and the run dies resolving
            // `skiko.mjs`. The same `commonTest` sources run on Android, iOS and the desktop, so
            // what a browser run would add is wasm codegen — worth having the day it packages.
            testTask { enabled = false }
        }
    }

    android {
        namespace = "com.begoml.bridge" + project.path.replace(":", ".").replace("-", "")
        compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest { }
        withDeviceTest { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
            implementation(libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}

dependencies {
    detektPlugins(project(":detekt-rules"))
}

detekt {
    parallel = true
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    basePath = rootProject.projectDir.absolutePath
}

// The Kotlin plugin feeds generated sources into its source sets; detekt must not judge them.
// Patterns are matched relative to each source root, and a generated root starts *below* build/,
// so the check has to look at the absolute path instead.
tasks.withType<Detekt>().configureEach {
    exclude { element -> element.file.absolutePath.contains("${File.separator}build${File.separator}") }
}

// The plain `detekt` task covers a single source set; this one covers every target the module has.
tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt over every Kotlin source set of this module."
    dependsOn(tasks.withType<Detekt>())
}
