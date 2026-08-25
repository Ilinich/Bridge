import io.gitlab.arturbosch.detekt.Detekt

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("io.gitlab.arturbosch.detekt")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    namespace = "com.begoml.bridge"
    compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()

    defaultConfig {
        applicationId = "com.begoml.bridge"
        minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
        targetSdk = libs.findVersion("android-targetSdk").get().requiredVersion.toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
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
tasks.withType<Detekt>().configureEach {
    exclude("**/build/**", "**/generated/**")
}

tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt over every Kotlin source set of this module."
    dependsOn(tasks.withType<Detekt>())
}
