#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/noise.glsl"

in vec3 vpos;
in vec2 vsize;
in vec3 vnormal;
in vec2 vtex;

#uniform float power;

#uniforms

out vec4 fragColor;

#import "$vprojection"

void main() {
    vec2 uv = vtex;

    float r = length(2.0 * (uv - 0.5));
    float a = (1.0 - r * r) * (1.0 - power);
    float ripple1 = fbm(uv * 0.25 + time * 0.04) * 3.5;
    float ripple2 = fbm(uv * 0.25 - time * 0.04) * 3.5;
    float n = (ripple1 + ripple2 - 2.0) * a;

    if (n < 0.2)
        discard;

    vec3 color = vec3(2.0*n, 0.5*n+n*n-0.4, 3.0*n-2.5);

    fragColor = vec4(color * n, n - 0.2);

    float zoffset = sqrt(vtex.x - vtex.x*vtex.x) * vsize.x * 1.5f + ripple1;
    vec4 vclip = pluginVProjection((view * vec4(vpos, 1.0) + vec4(0., 0., zoffset, 0.)).xyz);
    gl_FragDepth = 0.5 * vclip.z / vclip.w + 0.5;
}