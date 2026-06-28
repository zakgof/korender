#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 tex;

#ifdef VERTEX_TRANSFORM
    layout(location = 5) in vec4 instanceModel0;
    layout(location = 6) in vec4 instanceModel1;
    layout(location = 7) in vec4 instanceModel2;
    layout(location = 8) in vec4 instanceModel3;
#endif
#ifdef VERTEX_COLOR
    layout(location = 9) in vec4 color;
    out vec4 vcolor;
#endif
#ifdef VERTEX_METALLIC
    layout(location = 10) in float metallic;
    out float vmetallic;
#endif
#ifdef VERTEX_ROUGHNESS
    layout(location = 11) in float roughness;
    out float vroughness;
#endif
#ifdef VERTEX_COLORTEXINDEX
    layout(location = 12) in int colortexindex;
    flat out int vcolortexindex;
#endif

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;
#ifdef TRIPLANAR
    out vec3 vtriplanarpos;
    out vec3 vtriplanarnormal;
#endif

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

    #ifdef VERTEX_TRANSFORM
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

    #ifdef TRIPLANAR
        vec3 scale = vec3(
            length(totalModel[0].xyz),
            length(totalModel[1].xyz),
            length(totalModel[2].xyz)
        );
        vtriplanarpos = pos * scale;
        vtriplanarnormal = normalize(normal / scale);
    #endif

    #ifdef VERTEX_COLOR
        vcolor = color;
    #endif
    #ifdef VERTEX_METALLIC
        vmetallic = metallic;
    #endif
    #ifdef VERTEX_ROUGHNESS
        vroughness = roughness;
    #endif
    #ifdef VERTEX_COLORTEXINDEX
        vcolortexindex = colortexindex;
    #endif

    vpos = worldPos.xyz;
    vtex = tex;

    vec3 viewPos = (view * worldPos).xyz;
    gl_Position = pluginVProjection(viewPos);
}
