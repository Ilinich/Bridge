import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.detekt)
}

kotlin {
    jvmToolchain(21)

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "webApp"
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(projects.shared)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.koin.core)
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

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude { element -> element.file.absolutePath.contains("${File.separator}build${File.separator}") }
}

/** The aggregate task the root build expects of every module that has one. */
tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt over every Kotlin source set of this module."
    dependsOn(tasks.withType<io.gitlab.arturbosch.detekt.Detekt>())
}
