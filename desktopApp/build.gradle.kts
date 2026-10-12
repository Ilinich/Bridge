import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.detekt)
}

kotlin {
    jvmToolchain(21)
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(projects.shared)
            implementation(projects.core.background.api)
            implementation(libs.koin.core)
            // Dispatchers.Main on a desktop JVM is the AWT event queue, and the binding for it
            // ships separately; without it every state holder dies on its first main-thread hop.
            implementation(libs.kotlinx.coroutines.swing)
            implementation(compose.desktop.currentOs)
        }
    }
}

/** Renders every tab off screen; see Shots.kt for why this is not a UI test. */
tasks.register<JavaExec>("shots") {
    group = "verification"
    description = "Writes a PNG of each tab to /tmp."
    mainClass.set("com.begoml.bridge.shots.ShotsKt")
    args = (project.findProperty("shotArgs") as String?)?.split(" ").orEmpty()
    classpath = kotlin.jvm().compilations.getByName("main").runtimeDependencyFiles +
        kotlin.jvm().compilations.getByName("main").output.allOutputs
}

compose.desktop {
    application {
        mainClass = "com.begoml.bridge.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Bridge"
            packageVersion = "1.0.0"
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
