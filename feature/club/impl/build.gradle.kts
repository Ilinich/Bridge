plugins {
    id("bridge.kmp.compose")
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
            implementation(projects.navigation.core)
            implementation(projects.uikit)

            api(libs.moko.resources)
            implementation(libs.moko.resources.compose)
            implementation(libs.compose.material3)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewModel)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
    }
}
