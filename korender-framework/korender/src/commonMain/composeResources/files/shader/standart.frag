#import "shader/lib/header.glsl"
#import "shader/lib/texturing.glsl"
#import "shader/lib/light.glsl"
#import "shader/lib/pbr.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

#ifdef SHADOW_RECEIVER0
in vec3 vshadow0;
#endif
#ifdef SHADOW_RECEIVER1
in vec3 vshadow1;
#endif
#ifdef SHADOW_RECEIVER1
in vec3 vshadow2;
#endif

/////

uniform vec4 baseColor;
uniform float metallic;
uniform float roughness;
uniform vec4 emissiveFactor;

#ifdef ALBEDO_MAP
uniform sampler2D albedoTexture;
#endif
#ifdef METALLIC_ROUGHNESS_MAP
uniform sampler2D metallicRoughnessTexture;
#endif
#ifdef NORMAL_MAP
uniform sampler2D normalTexture;
#endif
#ifdef EMISSIVE_MAP
uniform sampler2D emissiveTexture;
#endif
#ifdef OCCLUSION_MAP
uniform sampler2D occlusionTexture;
#endif

// TODO TRIPLANAR AND APERIODIC FOR EVERYTHING

#ifdef TRIPLANAR
  uniform float triplanarScale;
#endif
#ifdef DETAIL
  uniform sampler2D detailTexture;
  uniform float detailScale;
  uniform float detailRatio;
#endif
#ifdef SHADOW_RECEIVER0
  uniform sampler2D shadowTexture0;
#endif
#ifdef SHADOW_RECEIVER1
  uniform sampler2D shadowTexture1;
#endif
#ifdef SHADOW_RECEIVER2
  uniform sampler2D shadowTexture2;
#endif

uniform vec3 cameraPos;
uniform vec3 light;

#ifdef PLUGIN_TEXTURE
  #import "$texture"
#endif

out vec4 fragColor;

void main() {

#ifdef ALBEDO_MAP
    vec4 albedo = texture(albedoTexture, vtex) * baseColor;
#else
    vec4 albedo = baseColor;
#endif
#ifdef METALLIC_ROUGHNESS_MAP
    vec4 mrtexel = texture(metallicRoughnessTexture, vtex);
    float metal = mrtexel.b * metallic;
    float rough = mrtexel.g * roughness;
#else
    float metal = metallic;
    float rough = roughness;
#endif
#ifdef EMISSIVE_MAP
    vec3 emissive = texture(emissiveTexture, vtex).rgb * emissiveFactor.rgb;
#else
    vec3 emissive = vec3(0.,0.,0.);
#endif
#ifdef OCCLUSION_MAP
    float occlusion = texture(emissiveTexture, vtex).r;
#else
    float occlusion = 1.;
#endif

    vec3 F0 = mix(vec3(0.04), albedo.rgb, metal);

#ifdef NORMAL_MAP
    vec3 N = getNormalFromMap(normalTexture, vnormal, vtex, vpos);
#else
    vec3 N = normalize(vnormal);
#endif

    vec3 V = normalize(cameraPos - vpos);
    vec3 L = normalize(-light);

    vec3 lightColor = vec3(10.0, 10.0, 10.0);
    float ambientFactor = 0.4;

    float shadowRatio = 0.;

#ifdef SHADOW_RECEIVER0
    shadowRatio = max(shadowRatio, shadow(shadowTexture0, vshadow0));
#endif
#ifdef SHADOW_RECEIVER1
    shadowRatio = max(shadowRatio, shadow(shadowTexture1, vshadow1));
#endif
#ifdef SHADOW_RECEIVER2
    shadowRatio = max(shadowRatio, shadow(shadowTexture2, vshadow2));
#endif

#ifdef NO_LIGHT
    vec3 radiance = albedo.rgb;
#else
    lightColor = lightColor * (1. - shadowRatio);
    vec3 ambient = ambientFactor * albedo.rgb * occlusion;
    vec3 radiance = ambient + emissive + lightColor * calculatePBR(N, V, L, F0, albedo.rgb, metal, rough, occlusion);
#endif

#ifdef SHADOW_CASTER
    fragColor = vec4(gl_FragCoord.z, gl_FragCoord.z, gl_FragCoord.z, 1.0);
#else
    fragColor = vec4(radiance, albedo.a);
#endif
}