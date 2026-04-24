vec3 skyibl(vec3 N, vec3 V, float roughness, vec3 albedo, vec3 F, vec3 kD, float NdotV) {
    vec3 R = reflect(-V, N);
    float maxBias = 8.0;
    vec3 diffuse = sky(N, maxBias) * albedo * 0.8;
    float DFG = mix(0.04, 1.0, pow(1.0 - roughness, 2.0));
    DFG *= mix(0.5, 1.0, NdotV);
    vec3 specular = sky(R, roughness * maxBias) * (F * DFG) * (1.0 + 0.3 * roughness);
    return diffuse * kD + specular;
}