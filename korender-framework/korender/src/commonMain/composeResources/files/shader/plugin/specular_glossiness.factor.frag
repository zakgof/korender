#uniform vec3 specularFactor;
#uniform float glossinessFactor;

vec4 pluginSpecularGlossiness() {
    return vec4(specularFactor, glossinessFactor);
}