#import "lib/header.glsl"
#import "lib/noise.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

uniform float time;
uniform float density;
uniform float seed;

uniform sampler2D noiseTexture;

out vec4 fragColor;

void main() {
    vec2 uv = vtex;
    float r = length(2. * (uv - 0.5));
    float a = density * clamp (1. - r*r, 0., 1.);
    float n = (fbmTex(noiseTexture, seed    + uv * 0.025 + time * 0.01) * 2. +
               fbmTex(noiseTexture, seed*2. + uv * 0.025 - time * 0.01) * 2. + 0.5) * a;
    fragColor = vec4(n, n, n, a);
}