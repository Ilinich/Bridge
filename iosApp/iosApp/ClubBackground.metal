#include <metal_stdlib>
#include <SwiftUI/SwiftUI_Metal.h>

using namespace metal;

// The club-blue wash, third dialect of one shader.
//
// The Compose app writes this once in the subset AGSL and SkSL share and runs it on Android and
// on iOS; a SwiftUI app has neither runtime, so the same maths is written again in Metal. The
// numbers are the source of truth and they are copied, not re-tuned: a wave across x, a sweep
// along x+y, and a dither of half a quantisation step, which is what keeps a gradient this
// shallow from banding on an 8-bit ramp.

static float grain(float2 p) {
    return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453);
}

[[ stitchable ]] half4 clubBackground(
    float2 position,
    half4 color,
    float time,
    float2 size
) {
    float2 uv = position / size;

    float wave = sin(uv.x * 3.1 + time * 0.6) * 0.5 + 0.5;
    float sweep = sin((uv.x + uv.y) * 2.2 - time * 0.35) * 0.5 + 0.5;
    float depth = smoothstep(0.0, 1.15, uv.y + wave * 0.22 + sweep * 0.10);

    half3 club = half3(0.012, 0.275, 0.580);
    half3 deep = half3(0.015, 0.063, 0.122);
    half3 lift = half3(0.243, 0.525, 0.910);

    half3 washed = mix(club, deep, half(depth));
    washed += lift * half(sweep * 0.06 * (1.0 - depth));

    float dither = (grain(floor(position) + floor(time * 20.0)) - 0.5) / 255.0;
    washed += half3(half(dither));

    return half4(washed, 1.0);
}
