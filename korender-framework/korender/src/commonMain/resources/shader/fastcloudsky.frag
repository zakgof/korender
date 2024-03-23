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
const vec3 fatcolor = vec3(0.25, 0.25, 0.3);
const float scale = 0.1;

void main() {

    vec2 uv = skydisk(vtex, cameraPos, projection * view, 2.5);

    float noise = fbmTex(noiseTexture, uv * scale)
          + 0.1 * fbmTex(noiseTexture, uv * scale * 5.0 - time * 0.001);

    float wh = pow(noise, 6.0) * 100.0;

    vec3 color = mix(skycolor, cloudcolor, clamp(wh, 0., 1.));

    fragColor = vec4(color, 1.0);
}