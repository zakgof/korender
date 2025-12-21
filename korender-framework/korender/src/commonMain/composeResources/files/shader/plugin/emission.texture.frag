uniform sampler2D emissionTexture;

vec3 pluginEmission() {
    return texture(emissionTexture, vtex).rgb;
}