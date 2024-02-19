#import "header.glsl"

in vec3 pos;
in vec2 tex;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform vec3 cameraPos;

uniform float xscale;
uniform float yscale;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

void main() {
    vec3 center = (model * vec4(pos, 1)).xyz;
    vec3 cameraRight = normalize(vec3(view[0][0], view[1][0], view[2][0]));
    vec3 cameraUp = normalize(vec3(view[0][1], view[1][1], view[2][1]));
    vpos = center + cameraRight * ((tex.x - 0.5) * xscale * model[0].x) + cameraUp * ((tex.y - 0.5) * yscale * model[1].y);
    vtex = tex;
    vnormal = normalize(cameraPos - center);

    gl_Position = projection * view * vec4(vpos, 1);
}