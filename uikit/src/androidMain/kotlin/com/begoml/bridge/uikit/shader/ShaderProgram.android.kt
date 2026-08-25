package com.begoml.bridge.uikit.shader

import android.graphics.RuntimeShader
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shader

internal actual class ShaderProgram actual constructor(source: String) {

    private val runtimeShader: RuntimeShader

    init {
        check(isRuntimeShaderSupported()) { "AGSL requires API 33" }
        runtimeShader = RuntimeShader(source)
    }

    actual fun shader(timeSeconds: Float, size: Size): Shader = runtimeShader.apply {
        setFloatUniform("uTime", timeSeconds)
        setFloatUniform("uResolution", size.width, size.height)
    }
}
