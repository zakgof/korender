#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/octa.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;
#ifdef VERTEX_COLOR
    in vec4 vcolor;
#endif
#ifdef VERTEX_OCCLUSION
    in float vocclusion;
#endif

#uniform vec4 baseColor;
#ifdef BASE_COLOR_MAP
    uniform sampler2D baseColorTexture;
#endif

#uniform float metallicFactor;
#uniform float roughnessFactor;

#uniforms

layout(location = 0) out vec4 albedoChannel;
layout(location = 1) out vec4 normalChannel;
layout(location = 2) out vec4 emissionChannel;

vec3 position;
vec4 albedo;
vec3 normal;
vec3 emission;
float occlusion;
float metallic;
float roughness;
vec3 color;

#ifdef PLUGIN_POSITION
#import "$position"
#endif

#ifdef PLUGIN_TEXTURING
#import "$texturing"
#endif

#ifdef PLUGIN_ALBEDO
#import "$albedo"
#endif

#ifdef PLUGIN_DISCARD
#import "$discard"
#endif

#ifdef PLUGIN_EMISSION
#import "$emission"
#endif

#ifdef PLUGIN_OCCLUSION
#import "$occlusion"
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

#ifdef PLUGIN_DEPTH
#import "$depth"
#endif

void main() {

    albedo = baseColor;

    #ifdef VERTEX_COLOR
        albedo *= vcolor;
    #endif

    #ifdef PLUGIN_POSITION
        position = pluginPosition();
    #else
        position = vpos;
    #endif

    #ifdef PLUGIN_TEXTURING
        albedo *= pluginTexturing();
    #else
        #ifdef BASE_COLOR_MAP
            albedo *= texture(baseColorTexture, vtex);
        #endif
    #endif

    #ifdef PLUGIN_NORMAL
        normal = pluginNormal();
    #else
        normal = normalize(vnormal);
    #endif

    #ifdef PLUGIN_ALBEDO
        albedo = pluginAlbedo();
    #endif

    #ifdef PLUGIN_DISCARD
        if (pluginDiscard())
            discard;
    #else
        if (albedo.a < 0.001)
            discard;
    #endif

    emission = vec3(0.);
    #ifdef PLUGIN_EMISSION
        emission = pluginEmission();
    #endif

    metallic = metallicFactor;
    roughness = roughnessFactor;

    #ifdef PLUGIN_METALLIC_ROUGHNESS
        vec2 mr = pluginMetallicRoughness();
        metallic = mr.x;
        roughness = mr.y;
    #endif

    #ifdef PLUGIN_SPECULAR_GLOSSINESS
        vec4 sg = pluginSpecularGlossiness();
        float maxSpec = max(max(sg.r, sg.g), sg.b);
        metallic = clamp((maxSpec - 0.04) / (1.0 - 0.04), 0.0, 1.0);
        if (metallic > 0.01) {
            albedo.rgb = sg.rgb;
        }
        roughness = 1. - sg.a;
    #endif

    occlusion = 1.;
    #ifdef VERTEX_OCCLUSION
        occlusion = vocclusion;
    #endif
    #ifdef PLUGIN_OCCLUSION
        occlusion *= pluginOcclusion();
    #endif

    ///////////////////////

    albedoChannel = vec4(albedo.rgb, metallic);
    normalChannel = vec4(normal * 0.5 + 0.5, roughness);
    emissionChannel = vec4(emission, occlusion);

    #ifdef PLUGIN_DEPTH
        gl_FragDepth = pluginDepth();
    #endif
}


