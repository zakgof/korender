#import "!shader/lib/header.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

uniform vec4 baseColor;
uniform vec4 emissiveFactor;
uniform float metallic;
uniform float roughness;

#ifdef SPECULAR_GLOSSINESS
    uniform vec4 specularFactor;
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

//  TODO DETAIL FOR BASECOLOR

uniform vec3 cameraPos;
uniform vec3 cameraDir;
uniform vec4 ambientColor;
uniform mat4 projection;
uniform mat4 view;

const int MAX_LIGHTS = 32;
uniform int numDirectionalLights;
uniform vec3 directionalLightDir[MAX_LIGHTS];
uniform vec4 directionalLightColor[MAX_LIGHTS];
uniform int directionalLightShadowTextureIndex[MAX_LIGHTS];
uniform int directionalLightShadowTextureCount[MAX_LIGHTS];

uniform int numPointLights;
uniform vec3 pointLightPos[MAX_LIGHTS];
uniform vec4 pointLightColor[MAX_LIGHTS];

const int MAX_SHADOWS = 12;
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

#ifdef PLUGIN_ALBEDO
#import "$albedo"
#endif

#ifdef PLUGIN_EMISSION
#import "$emission"
#endif

#import "!shader/lib/triplanar.glsl"
#import "!shader/lib/normalmap.glsl"

float shadowRatios[MAX_SHADOWS];

#import "!shader/lib/pbr.glsl"
#import "!shader/lib/shadow.glsl"

void main() {

#ifdef BASE_COLOR_MAP
    #ifdef TRIPLANAR
    vec4 albedo = triplanar(baseColorTexture, vpos * triplanarScale, vnormal) * baseColor;
    #else
    vec4 albedo = texture(baseColorTexture, vtex) * baseColor;
    #endif
#else
    vec4 albedo = baseColor;
#endif

#ifdef PLUGIN_ALBEDO
    albedo = pluginAlbedo(albedo);
#endif

#ifdef NORMAL_MAP
    vec3 N = getNormalFromMap(vnormal, vtex, vpos);
#else
    vec3 N = normalize(vnormal);
#endif

#ifdef EMISSIVE_MAP
    #ifdef TRIPLANAR
    vec3 emission = triplanar(emissiveTexture, vpos * triplanarScale, vnormal).rgb * emissiveFactor.rgb;
    #else
    vec3 emission = texture(emissiveTexture, vtex).rgb * emissiveFactor.rgb;
    #endif
#else
    vec3 emission = vec3(0.);
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
        vec3 specular = sgtexel.rgb * specularFactor.rgb;
        float glossiness = sgtexel.a * glossinessFactor;
    #else
        vec3 specular = specularFactor.rgb;
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

    vec3 color = c_diff * ambientColor.rgb + emission;

    float plane = dot((vpos - cameraPos), cameraDir);

    #ifdef OPENGL
        for (int s=0; s<numShadows; s++)
            shadowRatios[s] = casc(s, plane, shadowTextures[s]);
    #else
        if (numShadows > 0) shadowRatios[0] = casc(0, plane, shadowTextures[0]);
        if (numShadows > 1) shadowRatios[1] = casc(1, plane, shadowTextures[1]);
        if (numShadows > 2) shadowRatios[2] = casc(2, plane, shadowTextures[2]);
        if (numShadows > 3) shadowRatios[3] = casc(3, plane, shadowTextures[3]);
        if (numShadows > 4) shadowRatios[4] = casc(4, plane, shadowTextures[4]);
        if (numShadows > 5) shadowRatios[5] = casc(5, plane, shadowTextures[5]);
        if (numShadows > 6) shadowRatios[6] = casc(6, plane, shadowTextures[6]);
        if (numShadows > 7) shadowRatios[7] = casc(7, plane, shadowTextures[7]);
        if (numShadows > 8) shadowRatios[8] = casc(8, plane, shadowTextures[8]);
        if (numShadows > 9) shadowRatios[9] = casc(9, plane, shadowTextures[9]);
        if (numShadows > 10) shadowRatios[10] = casc(10, plane, shadowTextures[10]);
        if (numShadows > 11) shadowRatios[11] = casc(11, plane, shadowTextures[11]);
    #endif

    for (int l=0; l<numDirectionalLights; l++)
        color += dirLight(l, N, V, c_diff, F0, rough);

    for (int l=0; l<numPointLights; l++) {
        float shadowRatio = 0.;
        vec3 ftol = pointLightPos[l] - vpos;
        float distance = length(ftol);
        float att = 1.0 / (1.0 + 1.0 * distance + 1.0 * (distance * distance));
        vec3 lightValue = pointLightColor[l].rgb * (1. - shadowRatio) * att;// TODO quadratic; configurable attenuation ratio
        vec3 L = normalize(ftol);
        color += calculatePBR(N, V, L, c_diff, F0, rough, lightValue);
    }
    fragColor = vec4(color, albedo.a);
}