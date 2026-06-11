vec3 dirLight(int l, vec3 N, vec3 V, vec3 albedo, float metallic, float alpha2, float k, float ggxV, vec3 F0, float occlusion) {
    float shadowRatio = 0.;
    int shadowCount = directionalLightShadowTextureCount[l];
    for (int c=0; c<shadowCount; c++) {
        int idx = directionalLightShadowTextureIndex[l] + c;
        shadowRatio += shadowRatios[idx];
    }
    float lightCoef = occlusion * clamp(1.0 - shadowRatio, 0.0, 1.0);
    vec3 lightValue = directionalLightColor[l].rgb * lightCoef;
    vec3 L = -directionalLightDir[l];
    return calculatePBR(N, V, L, albedo, metallic, alpha2, k, ggxV, F0, lightValue);
}

vec3 dirLight(int l, vec3 N, vec3 V, vec3 albedo, float metallic, float roughness, float occlusion) {
    float alpha = roughness * roughness;
    float alpha2 = alpha * alpha;
    float k = (roughness + 1.0) * (roughness + 1.0) / 8.0;
    float NdotV = max(dot(N, V), 0.);
    float ggxV = NdotV / (NdotV * (1.0 - k) + k);
    vec3 F0 = mix(vec3(0.04), albedo, metallic);
    return dirLight(l, N, V, albedo, metallic, alpha2, k, ggxV, F0, occlusion);
}

vec3 pointLight(vec3 vpos, int l, vec3 N, vec3 V, vec3 albedo, float metallic, float alpha2, float k, float ggxV, vec3 F0, float occlusion) {
    float shadowRatio = 0.;
    vec3 ftol = pointLightPos[l] - vpos;
    float distance = length(ftol);
    vec3 attentuation = pointLightAttenuation[l];
    float att = 1.0 / (1.0 + attentuation.x * distance + attentuation.y * (distance * distance));
    float lightCoef = min(occlusion, 1. - shadowRatio);
    vec3 lightValue = pointLightColor[l].rgb * lightCoef * att;
    vec3 L = ftol / (distance + 0.0001);
    return calculatePBR(N, V, L, albedo, metallic, alpha2, k, ggxV, F0, lightValue);
}

vec3 pointLight(vec3 vpos, int l, vec3 N, vec3 V, vec3 albedo, float metallic, float roughness, float occlusion) {
    float alpha = roughness * roughness;
    float alpha2 = alpha * alpha;
    float k = (roughness + 1.0) * (roughness + 1.0) / 8.0;
    float NdotV = max(dot(N, V), 0.);
    float ggxV = NdotV / (NdotV * (1.0 - k) + k);
    vec3 F0 = mix(vec3(0.04), albedo, metallic);
    return pointLight(vpos, l, N, V, albedo, metallic, alpha2, k, ggxV, F0, occlusion);
}