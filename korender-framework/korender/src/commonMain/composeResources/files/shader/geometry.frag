#import "!shader/lib/header.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

uniform vec4 baseColor;
uniform float metallic;
uniform float roughness;

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

// TODO TRIPLANAR, DETAIL FOR EVERYTHIN

#ifdef PLUGIN_TEXTURE
  #import "$texture"
#endif

layout(location = 0) out vec4 albedoChannel;
layout(location = 1) out vec4 normalChannel;
layout(location = 2) out vec4 materialChannel;

#import "!shader/lib/texturing.glsl"
#import "!shader/lib/normals.glsl"

void main() {

#ifdef BASE_COLOR_MAP
    vec4 albedo = textureRegOrTriplanar(baseColorTexture, vtex, vpos, vnormal) * baseColor;
#else
    vec4 albedo = baseColor;
#endif

#ifdef PLUGIN_TEXTURE
    albedo = pluginTexture(albedo);
#endif

#ifdef NORMAL_MAP
    vec3 N = getNormalFromMap(normalTexture, vnormal, vtex, vpos);
#else
    vec3 N = normalize(vnormal);
#endif

#ifdef METALLIC_ROUGHNESS_MAP
    vec4 mrtexel = texture(metallicRoughnessTexture, vtex);
    float metal = mrtexel.b * metallic;
    float rough = mrtexel.g * roughness;
#else
    float metal = metallic;
    float rough = roughness;
#endif

    albedoChannel = albedo;
    normalChannel = vec4(N * 0.5 + 0.5, 1.0); // TODO vec3 RGB TODO unused channel
    materialChannel = vec4(metal, rough, 1.0, 1.0); // TODO vec3  RGB TODO unused channels
}