plugins {
    id("bridge.android.application")
    alias(libs.plugins.baselineProfile)
}

baselineProfile {
    mergeIntoMain = true
}

dependencies {
    implementation(projects.shared)

    // Applies the recorded profile on first run; without it the profile ships and never installs.
    implementation(libs.androidx.profileinstaller)
    baselineProfile(projects.benchmark)

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}
