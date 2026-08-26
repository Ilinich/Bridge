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

private const val FloodlightSource = """
uniform float uTime;
uniform float2 uResolution;

float grain(float2 p) {
    return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453);
}

float beam(float2 uv, float originX, float phase, float speed) {
    float2 toPixel = uv - float2(originX, -0.45);
    float angle = atan(toPixel.x, toPixel.y);
    float aim = sin(uTime * speed + phase) * 0.28;
    float cone = smoothstep(0.30, 0.0, abs(angle - aim));
    float reach = smoothstep(1.70, 0.10, length(toPixel));
    return cone * reach;
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / uResolution;

    half3 deep = half3(0.006, 0.035, 0.078);
    half3 club = half3(0.020, 0.259, 0.553);
    half3 color = mix(club, deep, half(smoothstep(-0.15, 1.05, uv.y)));

    float light = beam(uv, 0.18, 0.0, 0.55)
        + beam(uv, 0.50, 2.1, 0.42)
        + beam(uv, 0.82, 4.2, 0.63);
    color += half3(0.62, 0.78, 1.0) * half(light * 0.17);

    float flicker = grain(floor(fragCoord * 0.6) + floor(uTime * 20.0));
    color += half3(half((flicker - 0.5) * 0.05));

    float vignette = smoothstep(1.30, 0.30, length(uv - float2(0.5, 0.42)));
    color *= half(0.58 + 0.42 * vignette);

    return half4(color, 1.0);
}
"""

/**
 * Sweeping floodlights over the club crest.
 *
 * Three cones aim on independent sine phases, so the pattern never visibly repeats, and per-pixel
 * grain sits on top. Neither survives translation into a [Brush] — that is the point: this spec is
 * the screen that shows a runtime shader doing something a gradient cannot.
 */
val FloodlightShader: ShaderSpec = ShaderSpec(
    source = FloodlightSource,
    fallback = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B3E77),
            Color(0xFF072B57),
            Color(0xFF03101F),
        ),
    ),
)
