
vec3 doSpecularGlosiness(vec3 N, vec3 V, vec3 L, vec3 albedo, vec3 lightColor) {

#ifdef SPECULAR_GLOSSINESS_MAP
    vec4 sgtexel = textureRegOrTriplanar(specularGlossinessTexture, vtex, vpos, N);
    vec3 specular = sgtexel.rgb * specularFactor.rgb;
    float glossiness = sgtexel.a * glossinessFactor;
#else
    vec3 specular = specularFactor.rgb;
    float glossiness = glossinessFactor;
#endif

    vec3 H = normalize(V + L);

    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float NdotH = max(dot(N, H), 0.0);
    float VdotH = max(dot(V, H), 0.0);

    vec3 c_diff = albedo.rgb * (1. - max(max(specular.r, specular.g), specular.b));
    vec3 F0 = specular;
    float roughness = 1. - glossiness;

    vec3 F = F0 + (1. - F0) * pow(1. - VdotH, 5.);
    vec3 f_diffuse = (1. - F) * (1. / PI) * c_diff;

    float D = distributionGGX(NdotH, roughness);
    float G = geometrySmith(NdotV, NdotL, roughness);
    vec3 f_specular = F * D * G / max(4.0 * NdotV * NdotL, 0.000001);

    return (f_diffuse + f_specular) * NdotL * lightColor;
}