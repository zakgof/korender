uniform sampler2D metallicRoughnessTexture;

vec3 pluginMetallicRoughness() {
    return texture(metallicRoughnessTexture, vtex).bg;
}