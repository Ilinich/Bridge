package com.begoml.bridge.uikit.shader

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.asComposeShader
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

internal actual class ShaderProgram actual constructor(source: String) {

    private val builder = RuntimeShaderBuilder(RuntimeEffect.makeForShader(source))

    actual fun shader(timeSeconds: Float, size: Size): Shader {
        builder.uniform("uTime", timeSeconds)
        builder.uniform("uResolution", size.width, size.height)
        return builder.makeShader().asComposeShader()
    }
}
