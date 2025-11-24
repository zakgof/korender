vec3 skyibl(vec3 N, vec3 V, vec3 albedo, float metallic, float roughness) {
    vec3 R = reflect(-V, N);
    vec3 F0 = mix(vec3(0.04), albedo, metallic);
    float maxBias = 8.; // TODO ! Get from da sky
    vec3 envDiffuse = sky(N, maxBias) * albedo * (1.0 - metallic);
    vec3 envSpec = sky(R, roughness * maxBias) * fresnelSchlick(max(dot(V, N), 0.1), F0);
    return envDiffuse + envSpec;
}