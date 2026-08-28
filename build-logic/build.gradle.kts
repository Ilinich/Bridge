plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.plugin.android)
    implementation(libs.plugin.kotlinMultiplatform)
    implementation(libs.plugin.composeMultiplatform)
    implementation(libs.plugin.composeCompiler)
    implementation(libs.plugin.detekt)
}
