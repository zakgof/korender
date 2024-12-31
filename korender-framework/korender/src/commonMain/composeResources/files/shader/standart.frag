#import "!shader/lib/header.glsl"
#import "!shader/lib/texturing.glsl"
#import "!shader/lib/light.glsl"
#import "!shader/lib/shading.glsl"

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

#ifdef BASE_COLOR_MAP
uniform sampler2D baseColorTexture;
#endif
#ifdef NORMAL_MAP
uniform sampler2D normalTexture;
#endif

// PBR model

#ifdef PBR

    uniform float metallic;
    uniform float roughness;
    uniform vec4 emissiveFactor;

    #ifdef METALLIC_ROUGHNESS_MAP
    uniform sampler2D metallicRoughnessTexture;
    #endif
    #ifdef EMISSIVE_MAP
    uniform sampler2D emissiveTexture;
    #endif
    #ifdef OCCLUSION_MAP
    uniform sampler2D occlusionTexture;
    #endif

    #import "!shader/lib/pbr.glsl"

#endif

// Specular - glosiness model

#ifdef SPECULAR_GLOSSINESS

    uniform vec4 diffuseFactor;
    uniform vec4 specularFactor;
    uniform float glossinessFactor;

    #ifdef DIFFUSE_MAP
    uniform sampler2D diffuseTexture;
    #endif
    #ifdef SPECULAR_GLOSSINESS_MAP
    uniform sampler2D specularGlossinessTexture;
    #endif

    #import "!shader/lib/sg.glsl"

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

#ifdef BASE_COLOR_MAP
    vec4 albedo = texture(baseColorTexture, vtex) * baseColor;
#else
    vec4 albedo = baseColor;
#endif


#ifdef NO_LIGHT
    vec3 color = albedo.rgb;
#else

    #ifdef NORMAL_MAP
    vec3 N = getNormalFromMap(normalTexture, vnormal, vtex, vpos);
    #else
    vec3 N = normalize(vnormal);
    #endif

    vec3 V = normalize(cameraPos - vpos);
    vec3 L = normalize(-light);

    vec3 lightColor = vec3(10.0, 10.0, 10.0); // TODO magic number
    float ambientFactor = 0.4; // TODO magic number

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
    lightColor *= (1. - shadowRatio);

    #ifdef PBR
    vec3 color = doPbr(N, V, L, albedo.rgb, lightColor, ambientFactor);
    #endif
    #ifdef SPECULAR_GLOSSINESS
    vec3 color = doSpecularGlosiness(N, V, L, albedo.rgb, lightColor, ambientFactor);
    #endif

#endif

#ifdef SHADOW_CASTER
    fragColor = vec4(gl_FragCoord.z, gl_FragCoord.z, gl_FragCoord.z, 1.0);
#else
    fragColor = vec4(color, albedo.a);
#endif
}