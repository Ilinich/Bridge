plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.detekt.api)
    testImplementation(libs.detekt.test)
    testImplementation(libs.kotlin.test)
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
