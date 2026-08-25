package com.begoml.bridge.uikit.shader

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A runtime shader plus the gradient that stands in for it.
 *
 * [source] is written once in the dialect shared by AGSL and SkSL: `float2`/`half4` types and a
 * `half4 main(float2 fragCoord)` entry point. Every platform runtime receives two uniforms,
 * `uTime` in seconds and `uResolution` in pixels; a source that declares others will not link.
 *
 * [fallback] is not an error path. It renders wherever a runtime shader is unavailable, and it is
 * expected to read as a deliberate, quieter version of the same background.
 */
data class ShaderSpec(
    val source: String,
    val fallback: Brush,
)

private const val ClubBackgroundSource = """
uniform float uTime;
uniform float2 uResolution;

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / uResolution;

    float wave = sin(uv.x * 3.1 + uTime * 0.6) * 0.5 + 0.5;
    float sweep = sin((uv.x + uv.y) * 2.2 - uTime * 0.35) * 0.5 + 0.5;
    float depth = smoothstep(0.0, 1.15, uv.y + wave * 0.22 + sweep * 0.10);

    half3 club = half3(0.012, 0.275, 0.580);
    half3 deep = half3(0.015, 0.063, 0.122);
    half3 lift = half3(0.243, 0.525, 0.910);

    half3 color = mix(club, deep, half(depth));
    color += lift * half(sweep * 0.06 * (1.0 - depth));

    return half4(color, 1.0);
}
"""

/** The club-blue background used behind squad cards and the player pager. */
val ClubBackgroundShader: ShaderSpec = ShaderSpec(
    source = ClubBackgroundSource,
    fallback = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0A3A70),
            Color(0xFF062A56),
            Color(0xFF04101F),
        ),
    ),
)
