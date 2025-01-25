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

out vec4 fragColor;

#ifdef PLUGIN_TEXTURE
#import "$texture"
#endif

#import "!shader/lib/triplanar.glsl"
#import "!shader/lib/normalmap.glsl"

#import "!shader/lib/shadow.glsl"
#import "!shader/lib/pbr.glsl"

float calculateShadow(int i, vec3 v, int mode) {
    #ifdef WEBGL
    float sh = 0.;
    switch (i) {
        case 0: sh = shadow(shadowTextures[0], v, vpos, mode); break;
        case 1: sh =  shadow(shadowTextures[1], v, vpos, mode); break;
        case 2: sh =  shadow(shadowTextures[2], v, vpos, mode); break;
        case 3: sh =  shadow(shadowTextures[3], v, vpos, mode); break;
        case 4: sh =  shadow(shadowTextures[4], v, vpos, mode); break;
        case 5: sh =  shadow(shadowTextures[5], v, vpos, mode); break;
        case 6: sh =  shadow(shadowTextures[6], v, vpos, mode); break;
        case 7: sh =  shadow(shadowTextures[7], v, vpos, mode); break;
        case 8: sh =  shadow(shadowTextures[8], v, vpos, mode); break;
        case 9: sh =  shadow(shadowTextures[9], v, vpos, mode); break;
        case 10: sh =  shadow(shadowTextures[10], v, vpos, mode); break;
        case 11: sh =  shadow(shadowTextures[11], v, vpos, mode); break;
    }
    return sh;
    #else
    return shadow(shadowTextures[i], v, vpos, mode);
    #endif
}

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
    vec4 mrtexel = textureRegOrTriplanar(metallicRoughnessTexture, vtex, vpos, N);// TODO
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

    vec3 color = c_diff * ambientColor.rgb;

    float plane = dot((vpos - cameraPos), cameraDir);

    for (int l=0; l<numDirectionalLights; l++) {
        float shadowRatio = 0.;
        int shadowCount = directionalLightShadowTextureCount[l];
        for (int c=0; c<shadowCount; c++) {
            int idx = directionalLightShadowTextureIndex[l] + c;
            vec3 vshadow = (bsps[idx] * vec4(vpos, 1.0)).xyz;

            if ((shadowMode[idx] & 0x80) != 0) {
                vshadow.z = (yMax[idx] - vpos.y) / (yMax[idx] - yMin[idx]);
            }

//            vec2 poi = vec2(0.1, 0.5);
//            float dist = distance(poi, vshadow.xy);
//            float ratio = 0.50 * (1.0 - pow(dist, 3.));
//            vshadow.xy = vshadow.xy + (poi - vshadow.xy) * ratio;

            float sh = calculateShadow(idx, vshadow, shadowMode[idx] & 0x7);
            vec4 ci = cascade[c];
            float cascadeContribution = smoothstep(ci.r, ci.g, plane) * (1.0 - smoothstep(ci.b, ci.a, plane));
            shadowRatio += sh * cascadeContribution;
        }
        vec3 lightValue = directionalLightColor[l].rgb * (1. - shadowRatio);
        vec3 L = normalize(-directionalLightDir[l]);
        color += calculatePBR(N, V, L, c_diff, F0, rough, lightValue);
    }
    for (int l=0; l<numPointLights; l++) {
        float shadowRatio = 0.;
        vec3 ftol = pointLightPos[l] - vpos;
        float distance = length(ftol);
        float att = min(2.0, 20.0 / distance);
        vec3 lightValue = pointLightColor[l].rgb * (1. - shadowRatio) * att;// TODO quadratic; configurable attenuation ratio
        vec3 L = normalize(ftol);
        color += calculatePBR(N, V, L, c_diff, F0, rough, lightValue);
    }
    fragColor = vec4(color, albedo.a);
}