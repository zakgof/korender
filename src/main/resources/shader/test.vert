#version 130

in vec3 pos;
in vec3 normal;
in vec2 tex;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

uniform mat4 view;
uniform mat4 projection;

void main() {
    vec4 p = vec4(pos, 1.0);

    vpos = pos;
    vnormal = normal;
    vtex = tex;

    gl_Position = projection * (view * p);
}