vec3 calculatePBR(vec3 N, vec3 V, vec3 L, vec3 cdiff, vec3 F0, float roughness,
                  vec3 lightColor) {

    vec3 H = normalize(V + L);

    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float NdotH = max(dot(N, H), 0.0);
    float VdotH = max(dot(V, H), 0.0);

    vec3 F = F0 + (1. - F0) * pow(1. - VdotH, 5.);
    vec3 f_diffuse = (1. - F) * (1. / PI) * cdiff;

    float D = distributionGGX(NdotH, roughness);
    float G = geometrySmith(NdotV, NdotL, roughness);
    vec3 f_specular = F * D * G / max(4.0 * NdotV * NdotL, 0.000001);

    return (f_diffuse + f_specular) * NdotL * lightColor;
}