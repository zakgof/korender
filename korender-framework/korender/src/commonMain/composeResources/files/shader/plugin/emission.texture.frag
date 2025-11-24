uniform sampler2D emissiveTexture;
#uniform vec3 emissionFactor;

vec3 pluginEmission() {
    return texture(emissiveTexture, vtex).rgb * emissionFactor;
}