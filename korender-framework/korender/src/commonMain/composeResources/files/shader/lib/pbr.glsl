vec3 calculatePBR(vec3 N, vec3 V, vec3 L, vec3 F0, vec3 albedo, float metallic, float roughness, float occlusion) {
    vec3 H = normalize(V + L); // Halfway vector

    // Fresnel
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float NdotH = max(dot(N, H), 0.0);

    vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);

    // Normal Distribution Function (NDF)
    float D = distributionGGX(N, H, roughness);

    // Geometry function
    float G = geometrySmith(N, V, L, roughness);

    // Specular BRDF
    vec3 specular = (D * G * F) / max(4.0 * NdotV * NdotL, 0.001);

    // Diffuse BRDF (Lambertian)
    vec3 kS = F;                   // Specular reflectance
    vec3 kD = vec3(1.0) - kS;      // Diffuse reflectance
    kD *= 1.0 - metallic;          // No diffuse for metals

    vec3 diffuse = (albedo / PI);

    // Final shading with occlusion

    return (kD * diffuse + specular) * NdotL * occlusion ;
}


vec3 doPbr(vec3 N, vec3 V, vec3 L, vec3 albedo, vec3 lightColor, float ambientFactor) {

#ifdef METALLIC_ROUGHNESS_MAP
    vec4 mrtexel = texture(metallicRoughnessTexture, vtex);
    float metal = mrtexel.b * metallic;
    float rough = mrtexel.g * roughness;
#else
    float metal = metallic;
    float rough = roughness;
#endif
#ifdef EMISSIVE_MAP
    vec3 emissive = texture(emissiveTexture, vtex).rgb * emissiveFactor.rgb;
#else
    vec3 emissive = vec3(0.);
#endif
#ifdef OCCLUSION_MAP
    float occlusion = texture(emissiveTexture, vtex).r;
#else
    float occlusion = 1.;
#endif

    vec3 F0 = mix(vec3(0.04), albedo, metal);
    vec3 ambient = ambientFactor * albedo * occlusion;
    vec3 radiance = ambient + emissive + lightColor * calculatePBR(N, V, L, F0, albedo, metal, rough, occlusion);

    return radiance;
}
