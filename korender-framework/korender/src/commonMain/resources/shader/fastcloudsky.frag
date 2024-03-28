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

void main() {

    float density = 1.0; // 0..2 TODO: uniform

    vec3 look = screentolook(vtex, projection * view, cameraPos);
    vec2 uvorig = skydiskfromlook(look, 2.5) + time * 0.0005;

    vec2 uv = uvorig - 0.02 * fbmTex(noiseTexture, uvorig * 1.5);
    float f = fbmTex(noiseTexture, uv * 0.2);
    f = density-1.0 + 6.0*f + 6.0*f*f;

    float g = clamp(fbmTex(noiseTexture, uvorig * 0.4 - time * 0.0003) + 0.6, 0., 1.);
    vec3 cloud = mix(vec3(1.0, 1.0, 1.0), vec3(0.92, 0.92, 0.92), clamp(f*g, 0., 3.));

    vec3 blue = mix(vec3(0.4, 0.7, 1.0), vec3(0.2, 0.4, 0.6), -look.y); // TODO light direction
    vec3 result = mix(blue, cloud, clamp(f, 0., 1.));
    fragColor = vec4(result, 1.0);


}