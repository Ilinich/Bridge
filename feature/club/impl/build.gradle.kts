plugins {
    id("bridge.kmp.library")
    alias(libs.plugins.mokoResources)
}

multiplatformResources {
    resourcesPackage.set("com.begoml.bridge.feature.club")
    resourcesClassName.set("ClubStrings")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.club.api)

            implementation(projects.core.analytics.api)
            implementation(projects.core.connectivity.api)
            implementation(projects.foundation.tessera)
            implementation(projects.foundation.coroutines)
            implementation(projects.core.domain)
            implementation(projects.foundation.format)

            api(libs.moko.resources)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.koin.core)
        }
    }
}
