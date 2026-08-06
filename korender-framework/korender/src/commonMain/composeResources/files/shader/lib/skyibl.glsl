uniform sampler2D brdfLut;

vec3 skyibl(vec3 N, vec3 V, float roughness, vec3 diffuseColor, vec3 F0, float NdotV) {
    vec3 R = reflect(-V, N);
    float maxBias = 8.0;
    vec2 brdf = texture(brdfLut, vec2(NdotV, roughness)).rg;
    return sky(R, roughness * maxBias) * (F0 * brdf.x + brdf.y);
}
