uniform samplerCube normalCubeTexture;

vec3 pluginNormal() {
    return texture(normalCubeTexture, radiantDir).rgb * 2. - 1.;
}