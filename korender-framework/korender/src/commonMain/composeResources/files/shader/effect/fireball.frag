#import "!shader/lib/header.glsl"
#import "!shader/lib/noise.glsl"

in vec3 vpos;
in vec2 vsize;
in vec3 vnormal;
in vec2 vtex;

uniform float time;
uniform float power;
uniform mat4 view;
uniform mat4 projection;

uniform sampler2D noiseTexture;

out vec4 fragColor;

void main() {
    vec2 uv = vtex;

    float r = length(2.0 * (uv - 0.5));
    float a = (1.0 - r * r) * (1.0 - power);
    float ripple1 = fbmTex(noiseTexture, uv * 0.05 + time * 0.01);
    float ripple2 = fbmTex(noiseTexture, uv * 0.05 - time * 0.01);
    float n = (ripple2 * 2.0 + ripple2 * 2.0 + 2.0) * a;

    if (a < 0.001)
        discard;

    vec3 color = vec3(3.0*n, 0.5*n+n*n-0.4, 3.0*n-2.5);

    fragColor = vec4(color, a);

    float zoffset = sqrt(vtex.x - vtex.x*vtex.x) * vsize.x * 1.5f + ripple1;
    vec4 vclip = projection * (view * vec4(vpos, 1.0) + vec4(0., 0., zoffset, 0.));
    gl_FragDepth = 0.5 * vclip.z / vclip.w + 0.5;
}