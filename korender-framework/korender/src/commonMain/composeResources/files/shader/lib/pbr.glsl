vec3 calculatePBR(vec3 N, vec3 V, vec3 L, vec3 albedo, float metallic, float roughness,
                  vec3 lightColor, float occlusion, vec3 emissive) {

    vec3 H = normalize(V + L);

    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float NdotH = max(dot(N, H), 0.0);
    float VdotH = max(dot(V, H), 0.0);

    vec3 c_diff = mix(albedo, vec3(0.), metallic);
    vec3 F0 = mix(vec3(0.04), albedo, metallic);

    vec3 F = F0 + (1. - F0) * pow(1. - VdotH, 5.);
    vec3 f_diffuse = (1. - F) * (1. / PI) * c_diff;

    float D = distributionGGX(NdotH, roughness);
    float G = geometrySmith(NdotV, NdotL, roughness);
    vec3 f_specular = F * D * G / max(4.0 * NdotV * NdotL, 0.000001);

    return (f_diffuse + f_specular) * NdotL * lightColor;
}


vec3 doPbr(vec3 N, vec3 V, vec3 L, vec3 albedo, vec3 lightColor) {

#ifdef METALLIC_ROUGHNESS_MAP
    vec4 mrtexel = texture(metallicRoughnessTexture, vtex);
    float metal = mrtexel.b * metallic;
    float rough = mrtexel.g * roughness;
#else
    float metal = metallic;
    float rough = roughness;
#endif
#ifdef EMISSIVE_MAP
    vec3 emissive = textureRegOrTriplanar(emissiveTexture, vtex, vpos, N).rgb * emissiveFactor.rgb;
#else
    vec3 emissive = vec3(0.);
#endif
#ifdef OCCLUSION_MAP
    float occlusion = textureRegOrTriplanar(occlusionTexture, vtex, vpos, N).r;
#else
    float occlusion = 1.;
#endif

    return calculatePBR(N, V, L, albedo, metal, rough, lightColor, occlusion, emissive);
}
