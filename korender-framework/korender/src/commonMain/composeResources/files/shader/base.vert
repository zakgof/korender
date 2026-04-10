#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 tex;
#ifdef TEXTURE_ARRAY
    layout(location = 8) in int colortexindex;
    flat out int vcolortexindex;
#endif
#ifdef INSTANCING
//    layout(location = 11) in vec4 instanceModel0;
//    layout(location = 12) in vec4 instanceModel1;
//    layout(location = 13) in vec4 instanceModel2;
//    layout(location = 14) in vec4 instanceModel3;
    uniform sampler2D instTexture;
#endif

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

#uniform mat4 model;
#uniforms

mat4 totalModel;

#ifdef INSTANCING
    mat4 fetchInstancingMatrix(int instanceId) {
        vec4 col0 = texelFetch(instTexture, ivec2(0, instanceId), 0);
        vec4 col1 = texelFetch(instTexture, ivec2(1, instanceId), 0);
        vec4 col2 = texelFetch(instTexture, ivec2(2, instanceId), 0);
        vec4 col3 = texelFetch(instTexture, ivec2(3, instanceId), 0);
        return mat4(col0, col1, col2, col3);
    }
#endif

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
        totalModel = model * fetchInstancingMatrix(gl_InstanceID);
    #endif

    #ifdef PLUGIN_VPOSITION
        vec4 worldPos = pluginVPosition();
    #else
        vec4 worldPos = totalModel * vec4(pos, 1.0);
    #endif

    #ifdef PLUGIN_VNORMAL
        vnormal = pluginVNormal();
    #else
        vnormal =  mat3(totalModel) * normal; //mat3(transpose(inverse(totalModel))) * normal;
    #endif

    #ifdef TEXTURE_ARRAY
        vcolortexindex = colortexindex;
    #endif

    vpos = worldPos.xyz;
    vtex = tex;

    vec3 viewPos = (view * worldPos).xyz;
    gl_Position = pluginVProjection(viewPos);
}
