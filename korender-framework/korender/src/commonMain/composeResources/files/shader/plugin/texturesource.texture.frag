uniform sampler2D albedoTexture;

vec4 pluginTextureSource(vec2 uv) {
    return texture(albedoTexture, uv);
}

vec4 pluginTextureSourceGrad(vec2 uv, vec2 dx, vec2 dy) {
    return textureGrad(albedoTexture, uv, dx, dy);
}
