#import "!shader/lib/noisebias.glsl"

uniform samplerCube cubeTexture;

vec3 sky(vec3 look, float bias) {
    return texture(cubeTexture, look, bias).rgb;
}