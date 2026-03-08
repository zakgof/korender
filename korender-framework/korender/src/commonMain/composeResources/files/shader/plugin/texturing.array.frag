uniform sampler2DArray colorTextures;
flat in int vcolortexindex;

vec4 pluginTexturing() {
    return texture(colorTextures, vec3(vtex, float(vcolortexindex)));
}
