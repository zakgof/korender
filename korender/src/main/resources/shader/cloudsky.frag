#version 140

#import "cloudsky.glsl"

in vec2 vtex;
uniform float time;

uniform vec3 cameraPos;
uniform mat4 view;
uniform mat4 projection;

out vec4 fragColor;

void main() {
    vec3 look = screentolook(vtex, projection * view, cameraPos);
    fragColor = cloudsky(look, time);
}