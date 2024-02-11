#version 140

in vec3 pos;
in vec3 normal;
in vec2 tex;

out vec3 mpos;
out vec3 mnormal;
out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {

    vec4 worldPos = model * vec4(pos, 1.0);

    mpos = pos;
    mnormal = normal;
    vpos = worldPos.xyz;
    vtex = tex;
    vnormal = mat3(transpose(inverse(model))) * normal;

    gl_Position = projection * (view * worldPos);
}