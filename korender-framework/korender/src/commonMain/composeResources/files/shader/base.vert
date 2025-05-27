#import "!shader/lib/header.glsl"

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 tex;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform vec3 cameraPos;

#ifdef PLUGIN_VPOSITION
#import "$vposition"
#endif

#ifdef PLUGIN_VNORMAL
#import "$vnormal"
#endif

#ifdef PLUGIN_VOUTPUT
#import "$voutput"
#endif

void main() {

    #ifdef PLUGIN_VPOSITION
        vec4 worldPos = pluginVPosition();
    #else
        vec4 worldPos = model * vec4(pos, 1.0);
    #endif

    #ifdef PLUGIN_VNORMAL
        vnormal = pluginVNormal();
    #else
        vnormal = mat3(transpose(inverse(model))) * normal;
    #endif

    vpos = worldPos.xyz;
    vtex = tex;

    #ifdef PLUGIN_VOUTPUT
        gl_Position = pluginVOutput(worldPos);
    #else
        gl_Position = projection * (view * worldPos);
    #endif
}