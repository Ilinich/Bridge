package com.begoml.bridge.uikit.shader

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shader

/**
 * A compiled runtime-shader program.
 *
 * Compilation is the expensive half — both AGSL and SkSL parse and link the source when the
 * program is created — so a program is built once per [ShaderSpec] and only its uniforms change
 * per frame. Constructing one per frame costs roughly ten milliseconds of GPU time and is the
 * mistake this type exists to prevent.
 *
 * Construct only when [isRuntimeShaderSupported] is true.
 */
internal expect class ShaderProgram(source: String) {

    fun shader(timeSeconds: Float, size: Size): Shader
}
