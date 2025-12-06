uniform sampler2D metallicRoughnessTexture;

vec2 pluginMetallicRoughness() {
    return texture(metallicRoughnessTexture, vtex).bg;
}