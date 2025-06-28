#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/noise.glsl"

// Inspired by Xavier Benech https://www.shadertoy.com/view/XsXSWS

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

#uniform float strength;

#uniforms

out vec4 fragColor;

void main() {
    vec2 uv = vtex;
    vec2 q = vec2((uv.x - 0.5) * 0.4, 1.0 - (uv.y * 2. - 0.50));
    float n = fbm(0.1 * (strength * q - vec2(0, 3. * time))) * 1.4 - 0.4;
    float c = 1. - 7. * pow(max(0., length(q * vec2(1.8 + q.y * 1.5, .75)) - n * max(0., q.y + .25)), 1.2);
    c *= (n + 0.6);
    if (c < 0.001)
        discard;

    vec3 color = vec3(3.0*c, 0.5*c+c*c-0.4, 3.0*c-2.5);
    fragColor = vec4(color, c);
}