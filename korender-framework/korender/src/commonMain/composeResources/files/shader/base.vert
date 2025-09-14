#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 tex;
#ifdef INSTANCING
    layout(location = 11) in vec4 instanceModel0;
    layout(location = 12) in vec4 instanceModel1;
    layout(location = 13) in vec4 instanceModel2;
    layout(location = 14) in vec4 instanceModel3;
#endif

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

#uniform mat4 model;
#uniforms

mat4 totalModel;

#ifdef PLUGIN_VPOSITION
#import "$vposition"
#endif

#ifdef PLUGIN_VNORMAL
#import "$vnormal"
#endif

#import "$vprojection"

void main() {

    totalModel = model;

    #ifdef INSTANCING
        totalModel = model * mat4(instanceModel0, instanceModel1, instanceModel2, instanceModel3);
    #endif

    #ifdef PLUGIN_VPOSITION
        vec4 worldPos = pluginVPosition();
    #else
        vec4 worldPos = totalModel * vec4(pos, 1.0);
    #endif

    #ifdef PLUGIN_VNORMAL
        vnormal = pluginVNormal();
    #else
        vnormal = mat3(transpose(inverse(totalModel))) * normal;
    #endif

    vpos = worldPos.xyz;
    vtex = tex;

    vec3 viewPos = (view * worldPos).xyz;
    gl_Position = pluginVProjection(viewPos);
}