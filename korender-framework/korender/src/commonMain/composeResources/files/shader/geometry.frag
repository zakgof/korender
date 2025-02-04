#import "!shader/lib/header.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

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

uniform vec3 cameraPos;
uniform vec4 ambientColor;
uniform mat4 projection;
uniform mat4 view;

layout(location = 0) out vec3 cdiffChannel;
layout(location = 1) out vec3 normalChannel;
layout(location = 2) out vec4 materialChannel;
layout(location = 3) out vec3 emissionChannel;

#ifdef PLUGIN_ALBEDO
#import "$albedo"
#endif

#ifdef PLUGIN_EMISSION
#import "$emission"
#endif

#import "!shader/lib/triplanar.glsl"
#import "!shader/lib/normalmap.glsl"

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

    cdiffChannel = vec3(c_diff);
    normalChannel = vec3(N * 0.5 + 0.5);
    materialChannel = vec4(F0, rough);
    emissionChannel = vec3(emission);
}


