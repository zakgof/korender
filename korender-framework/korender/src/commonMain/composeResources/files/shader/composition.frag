#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D cdiffTexture;
uniform sampler2D normalTexture;
uniform sampler2D materialTexture;
uniform sampler2D depthTexture;

uniform vec3 cameraPos;
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

out vec4 fragColor;

#import "!shader/lib/light.glsl"
#import "!shader/lib/shading.glsl"
#import "!shader/lib/pbr.glsl"

float sampleShadowTexture(int i, vec3 v) {
    #ifdef WEBGL
    float sh = 0.;
    switch (i) {
        case 0: sh = shadow(shadowTextures[0], v); break;
        case 1: sh =  shadow(shadowTextures[1], v); break;
        case 2: sh =  shadow(shadowTextures[2], v); break;
        case 3: sh =  shadow(shadowTextures[3], v); break;
        case 4: sh =  shadow(shadowTextures[4], v); break;
        case 5: sh =  shadow(shadowTextures[5], v); break;
        case 6: sh =  shadow(shadowTextures[6], v); break;
        case 7: sh =  shadow(shadowTextures[7], v); break;
        case 8: sh =  shadow(shadowTextures[8], v); break;
        case 9: sh =  shadow(shadowTextures[9], v); break;
        case 10: sh =  shadow(shadowTextures[10], v); break;
        case 11: sh =  shadow(shadowTextures[11], v); break;
    }
    return sh;
    #else
    return shadow(shadowTextures[i], v);
    #endif
}

void main() {

    float depth = texture(depthTexture, vtex).r;

    vec4 ndcPosition;
    ndcPosition.xy = vtex * 2.0 - 1.0; // Convert to range [-1, 1]
    ndcPosition.z = depth * 2.0 - 1.0; // Depth range [-1, 1]
    ndcPosition.w = 1.0;
    vec4 viewPosition = inverse(projection) * ndcPosition; // TODO precalc inverse as uniform
    viewPosition /= viewPosition.w;
    vec4 worldPosition4 = inverse(view) * viewPosition;
    vec3 vpos = worldPosition4.xyz;

    vec3 c_diff = texture(cdiffTexture, vtex).rgb;
    vec4 materialTexel = texture(materialTexture, vtex);
    vec3 F0 = materialTexel.rgb;
    float roughness = materialTexel.a;

    vec3 V = normalize(cameraPos - vpos);
    vec3 N = normalize(texture(normalTexture, vtex).rgb * 2.0 - 1.0);

    vec3 color = c_diff * ambientColor.rgb;

    for (int l=0; l<numDirectionalLights; l++) {
        float shadowRatio = 0.;
        int shadowCount = directionalLightShadowTextureCount[l];
        for (int c=0; c<shadowCount; c++) {
            int idx = directionalLightShadowTextureIndex[l] + c;
            vec3 vshadow = (bsps[idx] * vec4(vpos, 1.0)).xyz;
            float sh = sampleShadowTexture(idx, vshadow);
            shadowRatio = max(shadowRatio, sh);
        }
        vec3 lightValue = directionalLightColor[l].rgb * (1. - shadowRatio);
        vec3 L = normalize(-directionalLightDir[l]);
        color += calculatePBR(N, V, L, c_diff, F0, roughness, lightValue);
    }
    for (int l=0; l<numPointLights; l++) {
        float shadowRatio = 0.;
        vec3 ftol = pointLightPos[l] - vpos;
        float distance = length(ftol);
        float att = max(2.0, 3.0 / distance);
        vec3 lightValue = pointLightColor[l].rgb * (1. - shadowRatio) * att;// TODO quadratic; configurable attenuation ratio
        vec3 L = normalize(ftol);
        color += calculatePBR(N, V, L, c_diff, F0, roughness, lightValue);
    }

    fragColor = vec4(color, 1.);
    gl_FragDepth = depth;
}