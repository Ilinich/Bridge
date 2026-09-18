plugins {
    id("bridge.kmp.library")
    alias(libs.plugins.mokoResources)
    alias(libs.plugins.skie)
}

multiplatformResources {
    resourcesPackage.set("com.begoml.bridge")
    resourcesClassName.set("HostStrings")
}

skie {
    // A build of this repository should not phone anywhere.
    analytics {
        enabled.set(false)
    }
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
            implementation(projects.core.features.following.impl)
            implementation(projects.core.analytics.impl)
            implementation(projects.foundation.logger.impl)
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(libs.coil.networkKtor)
            implementation(projects.feature.club.api)
            implementation(projects.feature.club.impl)
            implementation(projects.feature.matches.api)
            implementation(projects.feature.matches.impl)
            implementation(projects.feature.squad.api)
            implementation(projects.feature.player.impl)
            implementation(projects.feature.squad.impl)
            implementation(projects.navigation.routes)

            implementation(libs.koin.core)
            api(libs.moko.resources)
        }
    }
}

