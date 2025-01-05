#import "!shader/lib/header.glsl"
#import "!shader/lib/noise.glsl"

in vec3 vpos;
in vec2 vsize;
in vec2 vtex;

uniform float time;
uniform float density;
uniform float seed;
uniform mat4 view;
uniform mat4 projection;

uniform sampler2D noiseTexture;

out vec4 fragColor;

void main() {
    float r = length(2. * (vtex - 0.5));
    float a = density * clamp(1. - r * r, 0., 1.);
    float n = (fbmTex(noiseTexture, seed + vtex * 0.025 + time * 0.005)  +
               fbmTex(noiseTexture, seed * 2. + vtex * 0.025 - time * 0.005)) * a;
    n = clamp(n, 0., 1.);
    fragColor = vec4(0.3, 0.3, 0.3, 1.0) * n;

    float zoffset = sqrt(vtex.x - vtex.x * vtex.x) * vsize.x * 2.f;
    vec4 vclip = projection * (view * vec4(vpos, 1.) + vec4(0., 0., zoffset, 0.));
    gl_FragDepth = 0.5 * vclip.z / vclip.w + 0.5;
}