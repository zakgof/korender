#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

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
#uniform float alphaCutoff;

uniform sampler2D shadowTextures[5];
uniform sampler2DShadow pcfTextures[5];

#uniforms

out vec4 fragColor;

float shadowRatios[5];

vec3 position;
vec4 albedo;
vec3 normal;
vec3 emission;
float metallic;
float roughness;
vec3 color;
vec3 look;

#ifdef PLUGIN_POSITION
#import "$position"
#endif

#ifdef PLUGIN_TEXTURING
#import "$texturing"
#endif

#ifdef PLUGIN_NORMAL
#import "$normal"
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

#ifdef PLUGIN_METALLIC_ROUGHNESS
#import "$metallic_roughness"
#endif

#ifdef PLUGIN_SPECULAR_GLOSSINESS
#import "$specular_glossiness"
#endif

#ifdef PLUGIN_OUTPUT
#import "$output"
#endif

#ifdef PLUGIN_DEPTH
#import "$depth"
#endif

#import "!shader/lib/shadow.glsl"
#import "!shader/lib/pbr.glsl"
#import "!shader/lib/light.glsl"

#ifdef PLUGIN_SKY
#import "!shader/lib/space.glsl"
#import "!shader/lib/sky.glsl"
#import "$sky"
#import "!shader/lib/skyibl.glsl"
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
        if (albedo.a < alphaCutoff)
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

    ///////////////////////

    look = normalize(cameraPos - position);
    float plane = dot((position - cameraPos), cameraDir);

    float occlusion = 1.0;
    #ifdef VERTEX_OCCLUSION
        occlusion *= vocclusion;
    #endif

    populateShadowRatios(plane, position);

    color = ambientColor * albedo.rgb * (1.0 - metallic) + emission;

    for (int l=0; l<numDirectionalLights; l++) {
        color += dirLight(l, normal, look, albedo.rgb, metallic, roughness, occlusion);
    }
    for (int l=0; l<numPointLights; l++) {
        color += pointLight(vpos, l, normal, look, albedo.rgb, metallic, roughness, occlusion);
    }

    #ifdef PLUGIN_SKY
        float roughnessAA = antiAliasRoughness(roughness, normal, look);
        color += skyibl(normal, look, albedo.rgb, metallic, roughnessAA);
    #endif

    #ifdef PLUGIN_OUTPUT
        fragColor = pluginOutput();
    #else
        fragColor = vec4(color * albedo.a, albedo.a);
    #endif

    #ifdef PLUGIN_DEPTH
        gl_FragDepth = pluginDepth();
    #endif
}