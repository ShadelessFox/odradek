#version 330 core

in vec2 io_uv;
in vec3 io_color;

out vec4 out_color;

uniform sampler2D u_msdf;
uniform float u_distance_range;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float screenPxRange() {
    vec2 unitRange = vec2(u_distance_range);
    vec2 screenTexSize = vec2(1.0) / fwidth(io_uv);
    return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}

void main() {
    vec3 tex = texture(u_msdf, io_uv).rgb;
    float distance = median(tex.r, tex.g, tex.b);
    float range = screenPxRange();

    #define WEIGHT 1.0
    #define OUTLINE_OFFSET 0.7
    #define OUTLINE_COLOR vec3(0.0)
    #define BIAS 0.5

    // offset the SDF edge to make the font appear wider or slimmer
    float weight = (0.5 + (WEIGHT * -0.1));
    float characterDistance = range * (distance - weight);
    float outlineDistance = range * (distance - (weight - OUTLINE_OFFSET));

    // calculate how much color is required
    float characterColorAmount = clamp(characterDistance + 0.5, 0.0, 1.0);
    float outlineColorAmount = clamp(outlineDistance + 0.5, 0.0, 1.0);

    // mix between font color and outline color
    out_color.rgb = mix(OUTLINE_COLOR, io_color.rgb, characterColorAmount);

    // opacity is a max, so unreasonable outlines don't destroy the rendering
    out_color.a = pow(smoothstep(0.025, WEIGHT - OUTLINE_OFFSET, distance), BIAS);

    if (out_color.a < 0.01) {
        discard;
    }
}
