// Constants
const float PI = 3.14159265359;

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

vec3 calculatePBR(vec3 N, vec3 V, vec3 L, vec3 cdiff, vec3 F0, float roughness, vec3 lightColor) {

    vec3 H = normalize(V + L);

    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float NdotH = max(dot(N, H), 0.0);
    float VdotH = max(dot(V, H), 0.0);

    vec3 F = F0 + (1. - F0) * pow(1. - VdotH, 5.);
    vec3 f_diffuse = (1. - F) * (1. / PI) * cdiff;

#ifdef BLINN_PHONG
    float shininess = mix(2.0, 256.0, 1.0 - roughness);
    vec3 f_specular = F * pow(NdotH, shininess);
#else
    float D = distributionGGX(NdotH, roughness);
    float G = geometrySmith(NdotV, NdotL, roughness);
    vec3 f_specular = F * D * G / max(4.0 * NdotV * NdotL, 0.001);
#endif
    
#ifdef PLUGIN_SKY
    vec3 R = reflect(-V, N);
    float maxBias = 8.; // TODO ! Get from da sky
    vec3 env = sky(R, roughness * maxBias);
    vec3 FR = F0 + (1. - F0) * pow(1. - NdotV, 5.);
    vec3 indirect = env * FR;
#else
    vec3 indirect = vec3(0.);
#endif

    return (f_diffuse + f_specular) * NdotL * lightColor + indirect;
}