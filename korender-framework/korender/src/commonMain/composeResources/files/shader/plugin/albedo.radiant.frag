uniform samplerCube colorCubeTexture;

vec4 pluginAlbedo() {
    return texture(colorCubeTexture, radiantDir);
}