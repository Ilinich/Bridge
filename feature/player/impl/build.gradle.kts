plugins {
    id("bridge.kmp.library")
    alias(libs.plugins.mokoResources)
}

multiplatformResources {
    resourcesPackage.set("com.begoml.bridge.feature.player")
    resourcesClassName.set("PlayerStrings")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.player.api)

            implementation(projects.core.features.following.api)
            implementation(projects.foundation.tessera)
            implementation(projects.foundation.coroutines)
            implementation(projects.core.domain)
            api(libs.moko.resources)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.koin.core)
        }
    }
}
