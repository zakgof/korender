#import "!shader/lib/header.glsl"

layout(location = 0) in vec3 pos;
layout(location = 2) in vec2 tex;
layout(location = 6) in vec2 scale;
layout(location = 7) in float phi;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform vec3 cameraPos;

uniform float xscale;
uniform float yscale;
uniform float rotation;

out vec3 vpos;
out vec2 vsize;
out vec3 vnormal;
out vec2 vtex;

void main() {

    vec3 center = (model * vec4(pos, 1.0)).xyz;
    vec3 cameraRight = normalize(vec3(view[0][0], view[1][0], view[2][0]));
    vec3 cameraUp = normalize(vec3(view[0][1], view[1][1], view[2][1]));

    float right = ((tex.x - 0.5) * xscale * scale.x * model[0].x);
    float up = ((tex.y - 0.5) * yscale * scale.y * model[1].y);

    float l = sqrt(right * right + up * up);
    float angle = atan(up, right) + phi + rotation;

    vpos = center + cameraRight * l * cos(angle) + cameraUp * l * sin(angle);
    vsize = vec2(xscale * scale.x * model[0].x, yscale * scale.y * model[1].y);
    vtex = vec2(tex.x, 1.0 - tex.y);
    vnormal = normalize(cameraPos - center);

    gl_Position = projection * view * vec4(vpos, 1.0);
}