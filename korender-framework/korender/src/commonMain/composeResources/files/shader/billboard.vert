#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

layout(location = 2) in vec2 tex;
#ifdef INSTANCING
    layout(location = 11) in vec3 instpos;
    layout(location = 12) in vec2 instscale;
    layout(location = 13) in float instrot;
#endif

#uniform mat4 model;

#ifndef INSTANCING
    #uniform vec3 pos;
    #uniform vec2 scale;
    #uniform float rotation;
#endif

#uniforms

out vec3 vcenter;
out vec3 vpos;
out vec2 vsize;
out vec3 vnormal;
out vec2 vtex;

void main() {

    #ifdef INSTANCING
        vec3 bpos = instpos;
        vec2 bscale = instscale;
        float brot = instrot;
    #else
        vec3 bpos = pos;
        vec2 bscale = scale;
        float brot = rotation;
    #endif

    vcenter = bpos;
    vec3 cameraRight = normalize(vec3(view[0][0], view[1][0], view[2][0]));
    vec3 cameraUp = normalize(vec3(view[0][1], view[1][1], view[2][1]));

    float right = ((tex.x - 0.5) * bscale.x);
    float up = ((tex.y - 0.5) * bscale.y);

    float l = sqrt(right * right + up * up);
    float angle = atan(up, right) + brot;

    vpos = vcenter + cameraRight * l * cos(angle) + cameraUp * l * sin(angle);
    vsize = bscale;
    vtex = vec2(tex.x, 1.0 - tex.y);
    vnormal = normalize(cameraPos - vcenter);

    gl_Position = projection * view * vec4(vpos, 1.0);
}