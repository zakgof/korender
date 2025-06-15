#import "!shader/lib/header.glsl"

in vec3 vpos;

uniform sampler2D depthTexture;

//////////

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform vec3 cameraPos;
uniform vec2 renderSize;

uniform vec4 baseColor;
uniform sampler2D baseColorTexture;
uniform float metallicFactor;
uniform float roughnessFactor;

layout(location = 0) out vec4 decalDiffuse;
layout(location = 1) out vec4 decalNormal;
layout(location = 2) out vec4 decalMaterial;

#import "!shader/lib/space.glsl"

vec4 albedo;
vec3 normal;
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

#ifdef PLUGIN_METALLIC_ROUGHNESS
#import "$metallic_roughness"
#endif

#ifdef PLUGIN_SPECULAR_GLOSSINESS
#import "$specular_glossiness"
#endif

void main() {

    vec2 uv = gl_FragCoord.xy / renderSize;
    float depth = texture(depthTexture, uv).r;
    vec3 vpos = screenToWorldSpace(uv, depth);

    vec3 decalSpacePos = (inverse(model) * vec4(vpos, 1.0)).xyz + 0.5;
    if (decalSpacePos.x < 0. || decalSpacePos.x > 1. || decalSpacePos.y < 0. || decalSpacePos.y > 1.)
        discard;

    albedo = baseColor;

    #ifdef VERTEX_COLOR
        albedo *= vcolor;
    #endif

    #ifdef PLUGIN_TEXTURING
        albedo *= pluginTexturing();
    #else
        #ifdef BASE_COLOR_MAP
            albedo *= texture(baseColorTexture, decalSpacePos.xy);
        #endif
    #endif

    #ifdef PLUGIN_ALBEDO
        albedo = pluginAlbedo();
    #endif

    if (albedo.a < 0.001)
        discard;

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
    decalNormal = vec4(0.);
    decalMaterial = vec4(f0, roughness);
}