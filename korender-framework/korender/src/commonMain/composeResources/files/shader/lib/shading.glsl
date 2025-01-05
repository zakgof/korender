// Constants
const float PI = 3.14159265359;

// Helper functions
vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}

float distributionGGX(float NdotH, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;
    float NdotH2 = NdotH * NdotH;
    float num = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;
    return num / denom;
}

float geometrySchlickGGX(float NdotV, float roughness) {
    float r = (roughness + 1.0);
    float k = (r * r) / 8.0;
    float num = NdotV;
    float denom = NdotV * (1.0 - k) + k;
    return num / denom;
}

float geometrySmith(float NdotV, float NdotL, float roughness) {
    float ggx2 = geometrySchlickGGX(NdotV, roughness);
    float ggx1 = geometrySchlickGGX(NdotL, roughness);
    return ggx1 * ggx2;
}

vec3 getNormalFromMap(sampler2D normalMap, vec3 vnormal, vec2 vtex, vec3 vpos) {
    vec3 tangentNormal = texture(normalMap, vtex).rgb;
    tangentNormal = tangentNormal * 2.0 - 1.0; // Convert from [0,1] to [-1,1]

    vec3 Q1 = dFdx(vpos);
    vec3 Q2 = dFdy(vpos);
    vec2 st1 = dFdx(vtex);
    vec2 st2 = dFdy(vtex);

    vec3 N = normalize(vnormal);
    vec3 T = normalize(Q1 * st2.t - Q2 * st1.t);
    vec3 B = cross(N, T);

    mat3 TBN = mat3(T, B, N);
    return normalize(TBN * tangentNormal);
}