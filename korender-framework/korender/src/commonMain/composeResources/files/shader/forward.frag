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
uniform vec3 emissiveFactor;
uniform float metallic;
uniform float roughness;

#ifdef SPECULAR_GLOSSINESS
uniform vec3 specularFactor;
uniform float glossinessFactor;
#endif

#ifdef BASE_COLOR_MAP
uniform sampler2D baseColorTexture;
#endif

#ifdef NORMAL_MAP
uniform sampler2D normalTexture;
#endif

#ifdef EMISSIVE_MAP
uniform sampler2D emissiveTexture;
#endif

#ifdef TRIPLANAR
uniform float triplanarScale;
#endif

#ifdef DETAIL
uniform sampler2D detailTexture;
uniform float detailScale;
uniform float detailRatio;
#endif

#ifdef METALLIC_ROUGHNESS_MAP
uniform sampler2D metallicRoughnessTexture;
#endif
#ifdef SPECULAR_GLOSSINESS_MAP
uniform sampler2D specularGlossinessTexture;
#endif

#ifdef IBL
uniform samplerCube cubeTexture;
#endif

#ifdef TERRAIN
uniform vec3 tileOffsetAndScale;
uniform sampler2D heightTexture;
uniform int heightTextureSize;
uniform float heightScale;
uniform float outsideHeight;
uniform vec3 terrainCenter;
#endif

//  TODO DETAIL FOR BASECOLOR

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

#import "!shader/lib/triplanar.glsl"
#import "!shader/lib/normalmap.glsl"

float shadowRatios[MAX_SHADOWS];

#import "!shader/lib/shadow.glsl"
#import "!shader/lib/pbr.glsl"
#import "!shader/lib/light.glsl"

#ifdef TERRAIN
#import "!shader/lib/terrain.glsl"
#endif

#ifdef PLUGIN_ALBEDO
#import "$albedo"
#endif

#ifdef PLUGIN_EMISSION
#import "$emission"
#endif

void main() {

    #ifdef VERTEX_COLOR
    vec4 bcolor = baseColor * vcolor;
    #else
    vec4 bcolor = baseColor;
    #endif

    #ifdef BASE_COLOR_MAP
    #ifdef TRIPLANAR
    vec4 albedo = triplanar(baseColorTexture, vpos * triplanarScale, vnormal) * bcolor;
    #else
    vec4 albedo = texture(baseColorTexture, vtex) * bcolor;
    #endif
    #else
    vec4 albedo = bcolor;
    #endif

    #ifdef NORMAL_MAP
    vec3 N = getNormalFromMap(vnormal, vtex, vpos);
    #else
    vec3 N = normalize(vnormal);
    #endif

    #ifdef TERRAIN
    N = normalAt(vtex, float(heightTextureSize));
    #endif

    #ifdef PLUGIN_ALBEDO
    albedo = pluginAlbedo(vtex, vpos, N, albedo);
    #endif

    #ifdef EMISSIVE_MAP
    #ifdef TRIPLANAR
    vec3 emission = triplanar(emissiveTexture, vpos * triplanarScale, vnormal).rgb * emissiveFactor;
    #else
    vec3 emission = texture(emissiveTexture, vtex).rgb * emissiveFactor;
    #endif
    #else
    vec3 emission = emissiveFactor;
    #endif

    #ifdef PLUGIN_EMISSION
    emission = pluginEmission(emission);
    #endif

    #ifdef SPECULAR_GLOSSINESS
    #ifdef SPECULAR_GLOSSINESS_MAP
    #ifdef TRIPLANAR
    vec4 sgtexel = triplanar(specularGlossinessTexture, vtex, vpos, N);
    #else
    vec4 sgtexel = texture(specularGlossinessTexture, vtex);
    #endif
    vec3 specular = sgtexel.rgb * specularFactor;
    float glossiness = sgtexel.a * glossinessFactor;
    #else
    vec3 specular = specularFactor;
    float glossiness = glossinessFactor;
    #endif
    vec3 c_diff = albedo.rgb * (1. - max(max(specular.r, specular.g), specular.b));
    vec3 F0 = specular;
    float rough = 1. - glossiness;
    #else
    #ifdef METALLIC_ROUGHNESS_MAP
    #ifdef TRIPLANAR
    vec4 mrtexel = triplanar(metallicRoughnessTexture, vtex, vpos, N);
    #else
    vec4 mrtexel = texture(metallicRoughnessTexture, vtex);
    #endif
    float metal = mrtexel.b * metallic;
    float rough = mrtexel.g * roughness;
    #else
    float metal = metallic;
    float rough = roughness;
    #endif
    vec3 c_diff = mix(albedo.rgb, vec3(0.), metal);
    vec3 F0 = mix(vec3(0.04), albedo.rgb, metal);
    #endif

    ///////////////////////

    vec3 V = normalize(cameraPos - vpos);

    vec3 color = c_diff * ambientColor + emission;

    float plane = dot((vpos - cameraPos), cameraDir);

    #ifdef VERTEX_OCCLUSION
    float occlusion = vocclusion;
    #else
    float occlusion = 1.0;
    #endif

    populateShadowRatios(plane, vpos);

    for (int l=0; l<numDirectionalLights; l++) {
        color += dirLight(l, N, V, c_diff, F0, rough, occlusion);
    }
    for (int l=0; l<numPointLights; l++) {
        color += pointLight(vpos, l, N, V, c_diff, F0, rough, occlusion);
    }

    #ifdef DEPTH_TO_ALPHA
        albedo.a = gl_FragCoord.z;
    #endif

    fragColor = vec4(color, albedo.a);
}