#import "header.glsl"
#import "sky.glsl"
#import "noise.glsl"


in vec2 vtex;
uniform float time;

uniform vec3 cameraPos;
uniform mat4 view;
uniform mat4 projection;

out vec4 fragColor;

const vec3 skycolor = vec3(0.3, 0.6, 0.9);
const vec3 cloudcolor = vec3(1.0, 1.0, 1.0);
const vec3 fatcolor = vec3(0.5, 0.5, 0.6);
const float scale = 10.0;
const float density = 0.15;
const float sharpness = 30.0;


void main() {

    vec2 uv = skydisk(vtex, cameraPos, projection * view, 2.5);

    float noise = fbm(uv * scale + time * 0.02) + 0.1 * fbm(uv * scale * 10.0 - time * 0.1);

    float q = 1.0 - exp(-noise * sharpness - density);
    float q2 = 0.8 * pow(q, 4.0);

    vec3 color = mix(skycolor, cloudcolor, clamp(q + 0.2, 0., 1.));
    color = mix(color, fatcolor, clamp(q2, 0., 1.));

    fragColor = vec4(color, 1.0);
}