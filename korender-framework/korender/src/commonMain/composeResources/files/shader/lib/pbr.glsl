const float PI = 3.14159265359;

vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    float x = 1.0 - cosTheta;
    float x2 = x * x;
    float x5 = x2 * x2 * x;
    return F0 + (1.0 - F0) * x5;
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

vec3 calculatePBR(
    vec3 N, vec3 V, vec3 L, 
    vec3 albedo, float metallic, 
    float alpha2, float k, float ggxV, vec3 F0, 
    vec3 lightColor
) {
    vec3 H = normalize(V + L);

    float NdotV = max(dot(N, V), 0.);
    float NdotL = max(dot(N, L), 0.);
    float NdotH = max(dot(N, H), 0.);
    float VdotH = max(dot(V, H), 0.);

    // D = distributionGGX using alpha2
    float NdotH2 = NdotH * NdotH;
    float denomD = (NdotH2 * (alpha2 - 1.0) + 1.0);
    float D = alpha2 / (PI * denomD * denomD);

    // G = geometrySmith with specular visibility cancellation
    float denomG = 4.0 * (NdotV * (1.0 - k) + k) * (NdotL * (1.0 - k) + k);

    // F = fresnelSchlick
    float x = 1.0 - VdotH;
    float x2 = x * x;
    float x5 = x2 * x2 * x;
    vec3 F = F0 + (1.0 - F0) * x5;

    vec3 specular = (D * F) / denomG;
    vec3 diffuse = (1.0 - F) * (1.0 - metallic) * albedo / PI;
    return (diffuse + specular) * lightColor * NdotL;
}

vec3 calculatePBR(vec3 N, vec3 V, vec3 L, vec3 albedo, float metallic, float roughness, vec3 lightColor) {
    float alpha = roughness * roughness;
    float alpha2 = alpha * alpha;
    float k = (roughness + 1.0) * (roughness + 1.0) / 8.0;
    float NdotV = max(dot(N, V), 0.);
    float ggxV = NdotV / (NdotV * (1.0 - k) + k);
    vec3 F0 = mix(vec3(0.04), albedo, metallic);
    return calculatePBR(N, V, L, albedo, metallic, alpha2, k, ggxV, F0, lightColor);
}

float antiAliasRoughness(float roughness, vec3 N, vec3 V) {
    vec3 dndx = dFdx(N);
    vec3 dndy = dFdy(N);
    float varianceD = dot(dndx, dndx) + dot(dndy, dndy);
    return clamp(sqrt(roughness * roughness + varianceD * 4.0), 0., 1.);
}