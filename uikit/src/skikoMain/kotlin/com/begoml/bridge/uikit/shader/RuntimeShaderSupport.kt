package com.begoml.bridge.uikit.shader

/**
 * Every target Compose Multiplatform renders through Skia compiles SkSL — iOS, the desktop and
 * the browser alike. Android is the one that does not: it has AGSL and its own API version gate.
 */
internal actual fun isRuntimeShaderSupported(): Boolean = true
