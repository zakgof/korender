vec3 dirLight(int l, vec3 N, vec3 V, vec3 c_diff, vec3 F0, float rough, float occlusion) {
    float shadowRatio = 0.;
    int shadowCount = directionalLightShadowTextureCount[l];
    for (int c=0; c<shadowCount; c++) {
        int idx = directionalLightShadowTextureIndex[l] + c;
        shadowRatio += shadowRatios[idx];
    }
    float lightCoef = min(occlusion, 1. - shadowRatio);
    vec3 lightValue = directionalLightColor[l].rgb * lightCoef;
    vec3 L = normalize(-directionalLightDir[l]);
    return calculatePBR(N, V, L, c_diff, F0, rough, lightValue);
}

vec3 pointLight(vec3 vpos, int l, vec3 N, vec3 V, vec3 c_diff, vec3 F0, float rough, float occlusion) {
    float shadowRatio = 0.;
    vec3 ftol = pointLightPos[l] - vpos;
    float distance = length(ftol);
    vec3 attentuation = pointLightAttenuation[l];
    float att = 1.0 / (1.0 + attentuation.x * distance + attentuation.y * (distance * distance));
    float lightCoef = min(occlusion, 1. - shadowRatio);
    vec3 lightValue = pointLightColor[l].rgb * lightCoef * att;
    vec3 L = normalize(ftol);
    return calculatePBR(N, V, L, c_diff, F0, rough, lightValue);
}