#import "!shader/lib/header.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

out vec4 fragColor;

void main() {
    // TODO: fully transparent pixels should not cast shadows, get color, sample textures and cut
    // TODO: return moments for MSM, exp for ESM
    gl_FragDepth = gl_FragCoord.z;
}