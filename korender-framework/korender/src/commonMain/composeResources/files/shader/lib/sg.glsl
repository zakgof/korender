
vec3 doSpecularGlosiness(vec3 N, vec3 V, vec3 L, vec3 albedo, vec3 lightColor, float ambientFactor) {

#ifdef DIFFUSE_MAP
    vec4 diffuseColor = texture(diffuseTexture, vtex) * diffuseFactor;
#else
    vec4 diffuseColor = diffuseFactor;
#endif
#ifdef SPECULAR_GLOSSINESS_MAP
    vec4 sgtexel = texture(specularGlossinessTexture, vtex);
    vec3 specularColor = sgtexel.rgb * specularFactor.rgb;
    float glossiness = sgtexel.a * glossinessFactor;
#else
    vec3 specularColor = specularFactor.rgb;
    float glossiness = glossinessFactor;
#endif

    return diffuseColor.rgb;
}