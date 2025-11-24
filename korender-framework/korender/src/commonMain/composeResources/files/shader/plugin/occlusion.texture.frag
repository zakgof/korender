uniform sampler2D occlusionTexture;
#uniform float occlusionStrength;

float pluginOcclusion() {
    float occ = texture(occlusionTexture, vtex).r;
    return 1.0 + occlusionStrength * (occ - 1.0);
}