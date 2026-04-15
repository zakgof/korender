#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

layout(location = 2) in vec2 tex;
#ifdef VERTEX_POS
    layout(location = 5) in vec3 instpos;
#endif
#ifdef VERTEX_SCALE
    layout(location = 6) in vec2 instscale;
#endif
#ifdef VERTEX_ROT
    layout(location = 7) in float instrot;
#endif
#ifdef VERTEX_COLOR
    layout(location = 9) in vec4 instcolor;
    out vec4 vcolor;
#endif

#uniform mat4 model;

#uniform vec3 pos;
#uniform vec2 scale;
#uniform float rotation;

#uniforms

out vec3 vcenter;
out vec3 vpos;
out vec2 vsize;
out vec3 vnormal;
out vec2 vtex;

#import "$vprojection"

void main() {

    vec3 bpos = pos;
    vec2 bscale = scale;
    float brot = rotation;

    #ifdef VERTEX_POS
        bpos = bpos + instpos;
    #endif
    #ifdef VERTEX_SCALE
        bscale = bscale * instscale;
    #endif
    #ifdef VERTEX_ROT
        brot = brot + instrot;
    #endif
    #ifdef VERTEX_COLOR
        vcolor = instcolor;
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

    vec3 viewPos = (view * vec4(vpos, 1.0)).xyz;
    gl_Position = pluginVProjection(viewPos);
}