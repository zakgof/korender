uniform sampler2DArray colorTextures;
flat in int vcolortexindex;

vec4 pluginTextureSource(vec2 uv) {
    return vcolortexindex == -1 ? vec4(1.) : texture(colorTextures, vec3(uv, float(vcolortexindex)));
}

vec4 pluginTextureSourceGrad(vec2 uv, vec2 dx, vec2 dy) {
    return vcolortexindex == -1 ? vec4(1.) : textureGrad(colorTextures, vec3(uv, float(vcolortexindex)), dx, dy);
}