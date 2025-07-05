uniform sampler2D specularGlossinessTexture;

vec4 pluginSpecularGlossiness() {
    return texture(specularGlossinessTexture, vtex);
}