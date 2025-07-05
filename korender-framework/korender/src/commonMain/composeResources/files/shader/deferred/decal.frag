#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

uniform sampler2D depthGeometryTexture;

//////////

#uniform mat4 model;
#uniform vec2 renderSize;

#uniform vec4 baseColor;
#uniform float metallicFactor;
#uniform float roughnessFactor;

#uniforms

uniform sampler2D baseColorTexture;

layout(location = 0) out vec4 decalDiffuse;
layout(location = 1) out vec4 decalNormal;
layout(location = 2) out vec4 decalMaterial;

#import "!shader/lib/space.glsl"

vec2 vtex;
vec3 vpos;
vec3 vnormal;
vec4 albedo;
vec3 normal;
float normala;
float metallic;
float roughness;
vec3 diffuse;
vec3 f0;

#ifdef PLUGIN_TEXTURING
#import "$texturing"
#endif

#ifdef PLUGIN_ALBEDO
#import "$albedo"
#endif

#ifdef PLUGIN_NORMAL
#import "$normal"
#endif

#ifdef PLUGIN_METALLIC_ROUGHNESS
#import "$metallic_roughness"
#endif

#ifdef PLUGIN_SPECULAR_GLOSSINESS
#import "$specular_glossiness"
#endif

void main() {

    vec2 uv = gl_FragCoord.xy / renderSize;
    float depth = texture(depthGeometryTexture, uv).r;
    vpos = screenToWorldSpace(uv, depth);

    vtex = (inverse(model) * vec4(vpos, 1.0)).xy + 0.5;
    if (vtex.x < 0. || vtex.x > 1. || vtex.y < 0. || vtex.y > 1.)
        discard;

    vnormal = texture(depthGeometryTexture, uv).rgb * 2.0 - 1.0;

    albedo = baseColor;

    #ifdef VERTEX_COLOR
        albedo *= vcolor;
    #endif

    #ifdef PLUGIN_TEXTURING
        albedo *= pluginTexturing();
    #else
        #ifdef BASE_COLOR_MAP
            albedo *= texture(baseColorTexture, vtex);
        #endif
    #endif


    #ifdef PLUGIN_ALBEDO
        albedo = pluginAlbedo();
    #endif

    if (albedo.a < 0.001)
        discard;

    #ifdef PLUGIN_NORMAL
        normal = pluginNormal();
        normala = albedo.a;
    #else
        normal = vec3(0.);
        normala = 0.;
    #endif

    metallic = metallicFactor;
    roughness = roughnessFactor;

    #ifdef PLUGIN_METALLIC_ROUGHNESS
        vec2 mr = pluginMetallicRoughness();
        metallic = mr.x;
        roughness = mr.y;
    #endif

    diffuse = mix(albedo.rgb, vec3(0.), metallic);
    f0 = mix(vec3(0.04), albedo.rgb, metallic);

    decalDiffuse = vec4(diffuse, albedo.a);
    decalNormal = vec4((normal + 1.0) * 0.5, normala);
    decalMaterial = vec4(f0, roughness);
}