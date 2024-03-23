#import "header.glsl"
#import "sky.glsl"
#import "noise.glsl"


in vec2 vtex;
uniform float time;

uniform vec3 cameraPos;
uniform mat4 view;
uniform mat4 projection;

uniform sampler2D noiseTexture;

out vec4 fragColor;

const vec3 skycolor = vec3(0.3, 0.6, 0.9);
const vec3 cloudcolor = vec3(1.0, 1.0, 1.0);
const vec3 fatcolor = vec3(0.5, 0.5, 0.6);
const float scale = 10.0;
const float density = 0.10;
const float sharpness = 70.0;

void main() {

    vec2 uv = skydisk(vtex, cameraPos, projection * view, 2.5);

    float noise = 0.2 + 0.5 * fbmTex(noiseTexture, uv * scale + time * 0.1); // + 0.1 * fbmTex(noiseTexture, uv * scale * 10.0 - time * 0.1);

    float q = 0.4 - exp(-noise * sharpness - density);
    float q2 = 0.8 * pow(q, 4.0);

    vec3 color = mix(skycolor, cloudcolor, clamp(q, 0., 1.));
    color = mix(color, fatcolor, clamp(q2, 0., 1.));

    fragColor = vec4(noise, 0.0, 0.0, 1.0);
    //fragColor = vec4(color, 1.0);
}