uniform sampler3D volumeTexture;

vec4 pluginAlbedo() {
    return texture(volumeTexture, vec3(vtex, 0.5));
}