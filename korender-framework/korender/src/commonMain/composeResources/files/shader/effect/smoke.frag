#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/noise.glsl"

in vec3 vpos;
in vec2 vsize;
in vec2 vtex;

#uniform float density;
#uniform float seed;

#uniforms

out vec4 fragColor;

#import "$vprojection"

void main() {
    float r = length(2. * (vtex - 0.5));
    float a = density * clamp(1. - r * r, 0., 1.);
    float n = (fbm(seed +      vtex * 0.1 + time * 0.005) * 0.7 +
               fbm(seed * 2. + vtex * 0.2 - time * 0.005) * 0.7 - 0.66) * a;
    n = clamp(n, 0., 1.);
    fragColor = vec4(0.05, 0.05, 0.05, 1.0) * n;

    float zoffset = sqrt(vtex.x - vtex.x * vtex.x) * vsize.x * 2.f;
    vec4 vclip = pluginVProjection((view * vec4(vpos, 1.) + vec4(0., 0., zoffset, 0.)).xyz);
    gl_FragDepth = 0.5 * vclip.z / vclip.w + 0.5;
}