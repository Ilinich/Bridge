package com.begoml.bridge.core.favourites.impl

import org.koin.core.module.Module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

internal actual fun Module.bindFavouritesPath() {
    single { FavouritesPath(documentsPath()) }
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
private fun documentsPath(): String {
    val documents: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documents?.path) { "no documents directory" } + "/" + FavouritesFileName
}
