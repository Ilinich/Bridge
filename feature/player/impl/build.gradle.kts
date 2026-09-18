plugins {
    id("bridge.kmp.compose")
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
