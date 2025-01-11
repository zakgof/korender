#import "!shader/lib/header.glsl"
#import "!shader/lib/light.glsl"
#import "!shader/lib/shading.glsl"
#import "!shader/lib/pbr.glsl"

in vec2 vtex;

uniform sampler2D cdiffTexture;
uniform sampler2D normalTexture;
uniform sampler2D materialTexture;
uniform sampler2D depthTexture;

uniform vec3 cameraPos;
uniform vec4 ambientColor;
uniform mat4 projection;
uniform mat4 view;

struct DirectionalLight {
    vec3 dir;
    vec4 color;
    int shadowTextureIndex;
    int shadowTextureCount;
};
struct PointLight {
    vec3 pos;
    vec4 color;
};

uniform DirectionalLight directionalLights[32];
uniform int numDirectionalLights;
uniform PointLight pointLights[32];
uniform int numPointLights;
uniform sampler2D shadowTextures[12];
uniform mat4 bsps[12];

out vec4 fragColor;

float sampleShadowTexture(sampler2D texarray[12], int i, vec3 v) {
    float sh = 0.;
    switch (i) {
        case 0: sh = shadow(texarray[0], v); break;
        case 1: sh =  shadow(texarray[1], v); break;
        case 2: sh =  shadow(texarray[2], v); break;
        case 3: sh =  shadow(texarray[3], v); break;
        case 4: sh =  shadow(texarray[4], v); break;
        case 5: sh =  shadow(texarray[5], v); break;
        case 6: sh =  shadow(texarray[6], v); break;
        case 7: sh =  shadow(texarray[7], v); break;
        case 8: sh =  shadow(texarray[8], v); break;
        case 9: sh =  shadow(texarray[9], v); break;
        case 10: sh =  shadow(texarray[10], v); break;
        case 11: sh =  shadow(texarray[11], v); break;
    }
    return sh;
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

    vec3 V = normalize(cameraPos - vpos);

    vec4 cdiff = texture(cdiffTexture, vtex);
    vec3 N = normalize(texture(normalTexture, vtex).rgb * 2.0 - 1.0);

    vec4 materialTexel = texture(materialTexture, vtex);
    vec3 F0 = materialTexel.rgb;
    float roughness = materialTexel.a;

    vec3 color = cdiff.rgb * ambientColor.rgb;

    for (int l=0; l<numDirectionalLights; l++) {
        DirectionalLight dl = directionalLights[l];
        float shadowRatio = 0.;
        for (int c=0; c<dl.shadowTextureCount; c++) {
            int idx = dl.shadowTextureIndex + c;
            vec3 vshadow = (bsps[idx] * vec4(vpos, 1.0)).xyz;
            float sh = sampleShadowTexture(shadowTextures, idx, vshadow);
            shadowRatio = max(shadowRatio, sh);
        }
        vec3 lightValue = dl.color.rgb * (1. - shadowRatio);
        vec3 L = normalize(-dl.dir);
        color += calculatePBR(N, V, L, cdiff.rgb, F0, roughness, lightValue);
    }
    for (int l=0; l<numPointLights; l++) {
        float shadowRatio = 0.;
        vec3 ftol = pointLights[l].pos - vpos;
        float distance = length(ftol);
        float att = max(2.0, 3.0 / distance);
        vec3 lightValue = pointLights[l].color.rgb * (1. - shadowRatio) * att;// TODO quadratic; configurable attenuation ratio
        vec3 L = normalize(ftol);
        color += calculatePBR(N, V, L, cdiff.rgb, F0, roughness, lightValue);
    }

    fragColor = vec4(color, cdiff.a);

    gl_FragDepth = depth;
}