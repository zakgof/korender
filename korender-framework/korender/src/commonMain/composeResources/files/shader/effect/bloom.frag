#import "!shader/lib/header.glsl"

in vec2 vtex;

#uniforms

#uniform float threshold;

uniform sampler2D colorInputTexture;
uniform sampler2D depthInputTexture;
uniform sampler2D emissionGeometryTexture;

out vec4 fragColor;

float lumi(vec3 color) {
    return max(color.r, max(color.g, color.b));
}

void main() {
    vec3 color = texture(colorInputTexture, vtex).rgb;
    vec3 emission = texture(emissionGeometryTexture, vtex).rgb;

    float colorSoft = smoothstep(threshold - 0.1, threshold, lumi(color)); // TODO knee
    float emissionSoft = lumi(emission);

    float soft = max(colorSoft, emissionSoft);

    vec3 result = color * soft;

    fragColor = vec4(result, 1.0);
    gl_FragDepth = (soft < 0.1) ? 1.0 : texture(depthInputTexture, vtex).r;
}