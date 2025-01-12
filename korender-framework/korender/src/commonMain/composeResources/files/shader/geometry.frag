#import "!shader/lib/header.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

uniform vec4 baseColor;
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

layout(location = 0) out vec4 cdiffChannel;
layout(location = 1) out vec4 normalChannel;
layout(location = 2) out vec4 materialChannel;

#ifdef PLUGIN_TEXTURE
#import "$texture"
#endif

#import "!shader/lib/triplanar.glsl"
#import "!shader/lib/normalmap.glsl"

void main() {

    #ifdef BASE_COLOR_MAP
    #ifdef TRIPLANAR
    vec4 albedo = triplanarBaseColor(vpos * triplanarScale, vnormal) * baseColor;
    #else
    vec4 albedo = texture(baseColorTexture, vtex) * baseColor;
    #endif
    #else
    vec4 albedo = baseColor;
    #endif

    #ifdef PLUGIN_TEXTURE
    albedo = pluginTexture(albedo);
    #endif

    #ifdef NORMAL_MAP
    vec3 N = getNormalFromMap(vnormal, vtex, vpos);
    #else
    vec3 N = normalize(vnormal);
    #endif


    #ifdef SPECULAR_GLOSSINESS
    #ifdef SPECULAR_GLOSSINESS_MAP
    vec4 sgtexel = textureRegOrTriplanar(specularGlossinessTexture, vtex, vpos, N);
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
    vec4 mrtexel = textureRegOrTriplanar(metallicRoughnessTexture, vtex, vpos, N);
    float metal = mrtexel.b * metallic;
    float rough = mrtexel.g * roughness;
    #else
    float metal = metallic;
    float rough = roughness;
    #endif
    vec3 c_diff = mix(albedo.rgb, vec3(0.), metal);
    vec3 F0 = mix(vec3(0.04), albedo.rgb, metal);
    #endif

    cdiffChannel = vec4(c_diff, albedo.a); // TODO vec3 RGB TODO unused a channel
    normalChannel = vec4(N * 0.5 + 0.5, 1.0); // TODO vec3 RGB TODO unused a channel
    materialChannel = vec4(F0, rough);
}


