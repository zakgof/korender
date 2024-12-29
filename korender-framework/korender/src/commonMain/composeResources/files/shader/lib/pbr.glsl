// Constants
const float PI = 3.14159265359;

// Helper functions
vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}

float distributionGGX(vec3 N, vec3 H, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;
    float NdotH = max(dot(N, H), 0.0);
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

float geometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);

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

vec3 calculatePBR(vec3 N, vec3 V, vec3 L, vec3 F0, vec3 albedo, float metallic, float roughness, float occlusion) {
    vec3 H = normalize(V + L); // Halfway vector

    // Fresnel
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float NdotH = max(dot(N, H), 0.0);

    vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);

    // Normal Distribution Function (NDF)
    float D = distributionGGX(N, H, roughness);

    // Geometry function
    float G = geometrySmith(N, V, L, roughness);

    // Specular BRDF
    vec3 specular = (D * G * F) / max(4.0 * NdotV * NdotL, 0.001);

    // Diffuse BRDF (Lambertian)
    vec3 kS = F;                   // Specular reflectance
    vec3 kD = vec3(1.0) - kS;      // Diffuse reflectance
    kD *= 1.0 - metallic;          // No diffuse for metals

    vec3 diffuse = (albedo / PI);

    // Final shading with occlusion

    return (kD * diffuse + specular) * NdotL * occlusion ;
}
