#import "lib/header.glsl"
#import "lib/noise.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

uniform float time;
uniform float power;

uniform sampler2D noiseTexture;

out vec4 fragColor;

void main() {
    vec2 uv = vtex;

    float r = length(2.0 * (uv - 0.5));
    float a = (1.0 - r * r) * (1.0 - power);
    float n = (fbmTex(noiseTexture, uv * 0.05 + time * 0.01) * 2.0 +
               fbmTex(noiseTexture, uv * 0.05 - time * 0.01) * 2.0 + 2.0) * a;

     vec3 color = vec3(3.0*n, 0.5*n+n*n-0.4, 3.0*n-2.5);

    fragColor = vec4(color, a);
}