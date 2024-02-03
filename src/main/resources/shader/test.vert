#version 130

in vec3 pos;

uniform mat4 view;
uniform mat4 projection;

void main() {
    vec4 p = vec4(pos, 1.0);
    gl_Position = projection * (view * p);
}