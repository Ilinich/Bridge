plugins {
    id("bridge.kmp.compose")
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.analytics.api)
            implementation(projects.core.background.api)
            implementation(projects.core.background.impl)
            implementation(projects.core.connectivity.impl)
            implementation(projects.core.following.impl)
            implementation(projects.core.analytics.impl)
            implementation(projects.foundation.logger.impl)
            implementation(projects.core.data)
            implementation(libs.coil.compose)
            implementation(libs.coil.networkKtor)
            implementation(projects.feature.club.api)
            implementation(projects.feature.club.impl)
            implementation(projects.feature.matches.api)
            implementation(projects.feature.matches.impl)
            implementation(projects.feature.squad.api)
            implementation(projects.feature.player.impl)
            implementation(projects.feature.squad.impl)
            implementation(projects.navigation.core)
            implementation(projects.uikit)

            implementation(libs.compose.components.resources)
            implementation(libs.compose.material3)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiTooling)
            implementation(libs.compose.uiToolingPreview)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
