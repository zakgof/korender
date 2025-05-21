#import "!shader/lib/header.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;
#ifdef VERTEX_COLOR
    in vec4 vcolor;
#endif
#ifdef VERTEX_OCCLUSION
    in float vocclusion;
#endif

uniform vec4 baseColor;
#ifdef BASE_COLOR_MAP
    uniform sampler2D baseColorTexture;
#endif

uniform float metallicFactor;
uniform float roughnessFactor;

#ifdef IBL
uniform samplerCube cubeTexture;
#endif

uniform vec3 cameraPos;
uniform vec3 cameraDir;
uniform vec3 ambientColor;
uniform mat4 projection;
uniform mat4 view;

const int MAX_LIGHTS = 32;
uniform int numDirectionalLights;
uniform vec3 directionalLightDir[MAX_LIGHTS];
uniform vec3 directionalLightColor[MAX_LIGHTS];
uniform int directionalLightShadowTextureIndex[MAX_LIGHTS];
uniform int directionalLightShadowTextureCount[MAX_LIGHTS];

uniform int numPointLights;
uniform vec3 pointLightPos[MAX_LIGHTS];
uniform vec3 pointLightColor[MAX_LIGHTS];
uniform vec3 pointLightAttenuation[MAX_LIGHTS];

const int MAX_SHADOWS = 8;
uniform int numShadows;
uniform sampler2D shadowTextures[MAX_SHADOWS];
uniform mat4 bsps[MAX_SHADOWS];
uniform vec4 cascade[MAX_SHADOWS];
uniform float yMin[MAX_SHADOWS];
uniform float yMax[MAX_SHADOWS];
uniform int shadowMode[MAX_SHADOWS];
uniform float f1[MAX_SHADOWS];
uniform int i1[MAX_SHADOWS];

out vec4 fragColor;

float shadowRatios[MAX_SHADOWS];

#import "!shader/lib/shadow.glsl"
#import "!shader/lib/pbr.glsl"
#import "!shader/lib/light.glsl"

#ifdef PLUGIN_TEXTURING
#import "$texturing"
#endif

#ifdef PLUGIN_ALBEDO
#import "$albedo"
#endif

#ifdef PLUGIN_EMISSION
#import "$emission"
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

    vec4 albedo = baseColor;

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

    #ifdef PLUGIN_NORMAL
        vec3 normal = pluginNormal();
    #else
        vec3 normal = normalize(vnormal);
    #endif

    #ifdef PLUGIN_ALBEDO
        albedo = pluginAlbedo(albedo);
    #endif

    if (albedo.a < 0.001)
        discard;

    vec3 emission = vec3(0.);
    #ifdef PLUGIN_EMISSION
        emission = pluginEmission();
    #endif

    float metallic = metallicFactor;
    float roughness = roughnessFactor;

    #ifdef PLUGIN_METALLIC_ROUGHNESS
        vec2 mr = pluginMetallicRoughness();
        metallic = mr.x;
        roughness = mr.y;
    #endif

    vec3 diffuse = mix(albedo.rgb, vec3(0.), metallic);
    vec3 f0 = mix(vec3(0.04), albedo.rgb, metallic);

    #ifdef PLUGIN_SPECULAR_GLOSSINESS
        vec4 sg = pluginSpecularGlossiness();
        diffuse = albedo.rgb * (1. - max(max(sg.r, sg.g), sg.b));
        f0 = sg.rgb;
        roughness = 1. - sg.a;
    #endif

    ///////////////////////

    vec3 V = normalize(cameraPos - vpos);

    vec3 color = diffuse * ambientColor + emission;

    float plane = dot((vpos - cameraPos), cameraDir);

    float occlusion = 1.0;
    #ifdef VERTEX_OCCLUSION
        occlusion *= vocclusion;
    #endif

    populateShadowRatios(plane, vpos);

    for (int l=0; l<numDirectionalLights; l++) {
        color += dirLight(l, normal, V, diffuse, f0, roughness, occlusion);
    }
    for (int l=0; l<numPointLights; l++) {
        color += pointLight(vpos, l, normal, V, diffuse, f0, roughness, occlusion);
    }

    fragColor = vec4(color, albedo.a);

    #ifdef RADIANT_CAPTURE
        float radiant = length(cameraPos - vpos) / 15.0;
        fragColor = vec4(radiant, radiant, radiant, 1.0);
    #endif
    #ifdef NORMAL_CAPTURE
        fragColor = vec4((N + 1.) * 0.5, 1.0);
    #endif
}