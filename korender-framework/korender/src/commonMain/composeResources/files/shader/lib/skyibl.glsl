uniform sampler2D brdfLut;

vec3 skyibl(vec3 N, vec3 V, float roughness, vec3 diffuseColor, vec3 F0, float NdotV) {
    vec3 R = reflect(-V, N);
    float maxBias = 8.0;
    vec3 diffuse = sky(N, maxBias) * diffuseColor;
    vec2 brdf = texture(brdfLut, vec2(NdotV, roughness)).rg;
    vec3 specular = sky(R, roughness * maxBias) * (F0 * brdf.x + brdf.y);
    return diffuse + specular;
}
