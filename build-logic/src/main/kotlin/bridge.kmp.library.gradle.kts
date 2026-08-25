import io.gitlab.arturbosch.detekt.Detekt
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

    android {
        namespace = "com.begoml.bridge" + project.path.replace(":", ".").replace("-", "")
        compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest { }
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

detekt {
    parallel = true
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    basePath = rootProject.projectDir.absolutePath
}

// The Kotlin plugin feeds generated sources into its source sets; detekt must not judge them.
tasks.withType<Detekt>().configureEach {
    exclude("**/build/**", "**/generated/**")
}

// The plain `detekt` task covers a single source set; this one covers every target the module has.
tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt over every Kotlin source set of this module."
    dependsOn(tasks.withType<Detekt>())
}
