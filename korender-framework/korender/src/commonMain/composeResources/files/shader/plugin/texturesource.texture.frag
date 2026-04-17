uniform sampler2D colorTexture;

vec4 pluginTextureSource(vec2 uv) {
    return texture(colorTexture, uv);
}

vec4 pluginTextureSourceGrad(vec2 uv, vec2 dx, vec2 dy) {
    return textureGrad(colorTexture, uv, dx, dy);
}
