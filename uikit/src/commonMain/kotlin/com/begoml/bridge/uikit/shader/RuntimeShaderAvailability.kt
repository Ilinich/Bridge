package com.begoml.bridge.uikit.shader

/**
 * Whether this device can run a runtime shader at all.
 *
 * Screens should not branch on this — [rememberAnimatedShaderBrush] already returns the fallback
 * where it is false. It exists so diagnostics can report which path is live.
 */
fun isRuntimeShaderAvailable(): Boolean = isRuntimeShaderSupported()
