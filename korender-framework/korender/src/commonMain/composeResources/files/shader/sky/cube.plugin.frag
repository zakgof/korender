#import "!shader/lib/noise.glsl"

uniform samplerCube cubeTexture;

vec3 sky(vec3 look) {
    return texture(cubeTexture, look).rgb;
}