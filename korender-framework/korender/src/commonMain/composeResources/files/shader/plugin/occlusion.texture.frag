uniform sampler2D occlusionTexture;

float pluginOcclusion() {
    return texture(occlusionTexture, vtex).r;
}