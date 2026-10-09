package com.begoml.bridge.core.features.following.impl

import org.koin.core.module.Module
import java.io.File

internal actual fun Module.bindFollowingPath() {
    single { FollowingPath(dataDirectory().resolve(FollowingFileName).absolutePath) }
}

/** The OS data directory, not the working directory, which is wherever the launcher happened to be. */
private fun dataDirectory(): File {
    val home = File(System.getProperty("user.home"))
    val os = System.getProperty("os.name").lowercase()
    val base = when {
        os.contains("mac") -> File(home, "Library/Application Support")
        os.contains("win") -> File(System.getenv("APPDATA") ?: home.path)
        else -> File(System.getenv("XDG_DATA_HOME") ?: File(home, ".local/share").path)
    }
    return File(base, "Bridge").apply { mkdirs() }
}
