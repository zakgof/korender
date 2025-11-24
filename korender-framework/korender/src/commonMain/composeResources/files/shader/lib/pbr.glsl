const float PI = 3.14159265359;

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

vec3 calculatePBR(vec3 N, vec3 V, vec3 L, vec3 albedo, float metallic, float roughness, vec3 lightColor) {

    vec3 H = normalize(V + L);

    float NdotV = max(dot(N, V), 0.);
    float NdotL = max(dot(N, L), 0.);
    float NdotH = max(dot(N, H), 0.);
    float VdotH = max(dot(V, H), 0.);

    vec3 F0 = mix(vec3(0.04), albedo, metallic);

    float D = distributionGGX(NdotH, roughness);
    float G = geometrySmith(NdotV, NdotL, roughness);
    vec3 F = fresnelSchlick(VdotH, F0);

    vec3 specular = D * G * F / (4.0 * NdotV * NdotL + 0.001);
    vec3 diffuse = (1.0 - F) * (1.0 - metallic) * albedo / PI;
    return (diffuse + specular) * lightColor * NdotL;
}

float antiAliasRoughness(float roughness, vec3 N, vec3 V) {
    vec3 dndx = dFdx(N);
    vec3 dndy = dFdy(N);
    float varianceD = dot(dndx, dndx) + dot(dndy, dndy);
    return clamp(sqrt(roughness * roughness + varianceD * 4.0), 0., 1.);
}