vec3 skyibl(vec3 N, vec3 V, float roughness, vec3 diffFactor, vec3 specFactor) {
    vec3 R = reflect(-V, N);
    float maxBias = 8.; // TODO ! Get from da sky
    vec3 envDiffuse = sky(N, maxBias) * diffFactor;
    vec3 envSpec = sky(R, roughness * maxBias) * specFactor;
    return envDiffuse + envSpec;
}